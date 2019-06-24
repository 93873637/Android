/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <limits.h>
#include <poll.h>
#include <signal.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/inotify.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <sys/klog.h>
#include <time.h>
#include <unistd.h>
#include <sys/prctl.h>

#include <cutils/debugger.h>
#include <cutils/properties.h>
#include <cutils/sockets.h>
#include <private/android_filesystem_config.h>

//#include <selinux/android.h>
#define LOG_TAG "cm_log_collector"
#include <cutils/log.h>


static const int64_t NANOS_PER_SEC = 1000000000;

/* list of native processes to include in the native dumps */
static const char* native_processes_to_dump[] = {
        "/system/bin/drmserver",
        "/system/bin/mediaserver",
        "/system/bin/sdcard",
        "/system/bin/surfaceflinger",
        NULL,
};

static int64_t nanotime() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (int64_t)ts.tv_sec * NANOS_PER_SEC + ts.tv_nsec;
}

void cmti_dmesg(char *path) {
    printf("------ KERNEL LOG (dmesg) ------\n");
    /* Get size of kernel buffer */
    int size = klogctl(KLOG_SIZE_BUFFER, NULL, 0);
    if (size <= 0) {
        printf("Unexpected klogctl return value: %d\n\n", size);
        return;
    }
    char *buf = (char *) malloc(size + 1);
    if (buf == NULL) {
        printf("memory allocation failed\n\n");
        return;
    }
    int retval = klogctl(KLOG_READ_ALL, buf, size);
    if (retval < 0) {
        printf("klogctl failure\n\n");
        free(buf);
        return;
    }
    buf[retval] = '\0';

    FILE *fp = NULL;
    fp = fopen(path, "w");
    if (fp == NULL) {
        ALOGI("%s: %s\n", path, strerror(errno));
        exit(1);
    }
    fwrite(buf, 1, size+1, fp);
    fclose(fp);

    free(buf);
    return;
}

/* forks a command and do not waits for it to finish */
int run_command_no_wait(const char *title, const char *command, ...) {
    fflush(stdout);
    int64_t start = nanotime();
    pid_t pid = fork();

    /* handle error case */
    if (pid < 0) {
        printf("*** fork: %s\n", strerror(errno));
        return pid;
    }

    /* handle child case */
    if (pid == 0) {
        const char *args[1024] = {command};
        size_t arg;

        /* make sure the child dies when dumpstate dies */
        prctl(PR_SET_PDEATHSIG, SIGKILL);

        /* just ignore SIGPIPE, will go down with parent's */
        struct sigaction sigact;
        memset(&sigact, 0, sizeof(sigact));
        sigact.sa_handler = SIG_IGN;
        sigaction(SIGPIPE, &sigact, NULL);

        va_list ap;
        va_start(ap, command);
        if (title) printf("------ %s (%s", title, command);
        for (arg = 1; arg < sizeof(args) / sizeof(args[0]); ++arg) {
            args[arg] = va_arg(ap, const char *);
            if (args[arg] == NULL) break;
            if (title) printf(" %s", args[arg]);
        }
        if (title) printf(") ------\n");
        fflush(stdout);

        execvp(command, (char**) args);
        printf("*** exec(%s): %s\n", command, strerror(errno));
        fflush(stdout);
        _exit(-1);
    }

    /* handle parent case */
    //do nothing, just wait for 0.1s
    usleep(100000);
    return pid;
}

/* forks a command and waits for it to finish */
int run_command_redirect(const char *pathname, int timeout_seconds, const char *command, ...) {
    fflush(stdout);
    int64_t start = nanotime();
    pid_t pid = fork();
    int ret = -1;

    /* handle error case */
    if (pid < 0) {
        printf("*** fork: %s\n", strerror(errno));
        return pid;
    }

    /* handle child case */
    if (pid == 0) {
        const char *args[1024] = {command};
        size_t arg;
        int fd = -1;

        /* make sure the child dies when dumpstate dies */
        prctl(PR_SET_PDEATHSIG, SIGKILL);

        /* just ignore SIGPIPE, will go down with parent's */
        struct sigaction sigact;
        memset(&sigact, 0, sizeof(sigact));
        sigact.sa_handler = SIG_IGN;
        sigaction(SIGPIPE, &sigact, NULL);

        va_list ap;
        va_start(ap, command);
        for (arg = 1; arg < sizeof(args) / sizeof(args[0]); ++arg) {
            args[arg] = va_arg(ap, const char *);
            if (args[arg] == NULL) break;
        }

        fd = open(pathname, O_WRONLY | O_CREAT | O_TRUNC, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);

        if (fd == -1) {
            ALOGI("%s: %s\n", pathname, strerror(errno));
        }

        //redirect output to input file.
        ret = dup2(fd, STDOUT_FILENO);

        if (ret == -1) {
            ALOGI("%s: %s\n", "dup fail", strerror(errno));
        }

        execvp(command, (char**) args);
        printf("*** exec(%s): %s\n", command, strerror(errno));
        fflush(stdout);
        _exit(-1);
    }

    /* handle parent case */
    for (;;) {
        int status;
        pid_t p = waitpid(pid, &status, WNOHANG);
        int64_t elapsed = nanotime() - start;
        if (p == pid) {
            if (WIFSIGNALED(status)) {
                printf("*** %s: Killed by signal %d\n", command, WTERMSIG(status));
            } else if (WIFEXITED(status) && WEXITSTATUS(status) > 0) {
                printf("*** %s: Exit code %d\n", command, WEXITSTATUS(status));
            }
            return status;
        }

        if (timeout_seconds && elapsed / NANOS_PER_SEC > timeout_seconds) {
            printf("*** %s: Timed out after %ds (killing pid %d)\n", command, (int) elapsed, pid);
            kill(pid, SIGTERM);
            return -1;
        }

        usleep(100000);  // poll every 0.1 sec
    }
}

/* forks a command and waits for it to finish */
int run_command(const char *title, int timeout_seconds, const char *command, ...) {
    fflush(stdout);
    int64_t start = nanotime();
    pid_t pid = fork();

    /* handle error case */
    if (pid < 0) {
        printf("*** fork: %s\n", strerror(errno));
        return pid;
    }

    /* handle child case */
    if (pid == 0) {
        const char *args[1024] = {command};
        size_t arg;

        /* make sure the child dies when dumpstate dies */
        prctl(PR_SET_PDEATHSIG, SIGKILL);

        /* just ignore SIGPIPE, will go down with parent's */
        struct sigaction sigact;
        memset(&sigact, 0, sizeof(sigact));
        sigact.sa_handler = SIG_IGN;
        sigaction(SIGPIPE, &sigact, NULL);

        va_list ap;
        va_start(ap, command);
        if (title) printf("------ %s (%s", title, command);
        for (arg = 1; arg < sizeof(args) / sizeof(args[0]); ++arg) {
            args[arg] = va_arg(ap, const char *);
            if (args[arg] == NULL) break;
            if (title) printf(" %s", args[arg]);
        }
        if (title) printf(") ------\n");
        fflush(stdout);

        execvp(command, (char**) args);
        printf("*** exec(%s): %s\n", command, strerror(errno));
        fflush(stdout);
        _exit(-1);
    }

    /* handle parent case */
    for (;;) {
        int status;
        pid_t p = waitpid(pid, &status, WNOHANG);
        int64_t elapsed = nanotime() - start;
        if (p == pid) {
            if (WIFSIGNALED(status)) {
                printf("*** %s: Killed by signal %d\n", command, WTERMSIG(status));
            } else if (WIFEXITED(status) && WEXITSTATUS(status) > 0) {
                printf("*** %s: Exit code %d\n", command, WEXITSTATUS(status));
            }
            if (title) printf("[%s: %.3fs elapsed]\n\n", command, (float)elapsed / NANOS_PER_SEC);
            return status;
        }

        if (timeout_seconds && elapsed / NANOS_PER_SEC > timeout_seconds) {
            printf("*** %s: Timed out after %ds (killing pid %d)\n", command, (int) elapsed, pid);
            kill(pid, SIGTERM);
            return -1;
        }

        usleep(100000);  // poll every 0.1 sec
    }
}

/* forks a run command by setprop and waits for it to finish */
int run_command_by_prop(const char *title,
                        int timeout_seconds,
                        const char *property,
                        const char *command,
                        void *noused)
{
    noused=noused;
    ALOGD("run_command_by_prop property= %s command=%s\n",property,command);
    if (strncmp(property,"ctl.start",strlen(property)) != 0) {
        ALOGD("Bad property.\n");
        return -1;
        }

    fflush(stdout);
    int64_t start = nanotime();
    pid_t pid = fork();

    /* handle error case */
    if (pid < 0) {
        printf("*** fork: %s\n", strerror(errno));
        return pid;
    }

    /* handle child case */
    if (pid == 0) {
        char result[PROPERTY_VALUE_MAX];
        char result_prop[32];
        size_t arg;

        /* make sure the child dies when dumpstate dies */
        prctl(PR_SET_PDEATHSIG, SIGKILL);

        /* just ignore SIGPIPE, will go down with parent's */
        struct sigaction sigact;
        memset(&sigact, 0, sizeof(sigact));
        sigact.sa_handler = SIG_IGN;
        sigaction(SIGPIPE, &sigact, NULL);

        property_set("ctl.start", command);
        sleep(1);

        snprintf(result_prop,32,"%s.%s","init.svc",command);
        while(1){
            property_get(result_prop,result,"0");
            if (strncmp(result,"stopped",7) == 0){
                break;
            }
            sleep(1);
        }
        ALOGD("run command = %s succ\n", command);
        fflush(stdout);
        _exit(-1);
    }

    /* handle parent case */
    for (;;) {
        int status;
        pid_t p = waitpid(pid, &status, WNOHANG);
        int64_t elapsed = nanotime() - start;
        if (p == pid) {
            if (WIFSIGNALED(status)) {
                printf("*** %s: Killed by signal %d\n", command, WTERMSIG(status));
            } else if (WIFEXITED(status) && WEXITSTATUS(status) > 0) {
                printf("*** %s: Exit code %d\n", command, WEXITSTATUS(status));
            }
            if (title) printf("[%s: %.3fs elapsed]\n\n", command, (float)elapsed / NANOS_PER_SEC);
            return status;
        }

        if (timeout_seconds && elapsed / NANOS_PER_SEC > timeout_seconds) {
            printf("*** %s: Timed out after %ds (killing pid %d)\n", command, (int) elapsed, pid);
            kill(pid, SIGTERM);
            return -1;
        }

        usleep(100000);  // poll every 0.1 sec
    }
}


void save_last_info(char* pDir) {
	char cmd[128];
	char tmp[128];
	if (!access(pDir, F_OK)){
		snprintf(tmp,128,"%s/%s",pDir,"last_kmsg.txt");
		ALOGD("save_last_info into %s",tmp);
		snprintf(cmd,128,"%s%s","cat /sys/fs/pstore/console-ramoops-0 > ",tmp);
		system(cmd);
	}
	else
		ALOGE("save_last_info into %s can not be accessed.",pDir);
}

void save_prop_info(char* pDir) {
	char cmd[128];
	char tmp[128];
	if (!access(pDir, F_OK)){
		snprintf(tmp,128,"%s/%s",pDir,"prop_info.txt");
		ALOGD("save_prop_info into %s",tmp);
		snprintf(cmd,128,"%s%s","getprop > ",tmp);
		system(cmd);
	}
	else
		ALOGE("save_prop_info into %s can not be accessed.",pDir);
}
int create_log_dir(char* pDir, mode_t mode) {
    int i = 0;
    int iRet;
    int iLen;
    char* pszDir;

    if (NULL == pDir) {
        return -1;
    }

    pszDir = strdup(pDir);
    iLen = strlen(pszDir);
    //ALOGD("pszDir %s\n", pszDir);
    // create dir
    for (i = 0; i < iLen+1; i++) {
        if ((pszDir[i] == '/' && i != 0) || pszDir[i] == '\0') {
            pszDir[i] = '\0';
            if(strncmp(pszDir,"/sdcard",strlen(pszDir))!=0)
            {
                iRet = access(pszDir, 0);
                if (iRet != 0) {
                    iRet = mkdir(pszDir, mode);
                    if (iRet != 0) {
                        ALOGE("create dir %s failed with error %s",pszDir,strerror(errno));
                        return -1;
                    }
                }
            }
            pszDir[i] = '/';
        }
    }
    return iRet;
}

int CopyFile(const char *pDst, const char *pSrc)
{
    int ret = 1;
    FILE *fpr = NULL;
    FILE *fpw = NULL;
    char* buf = NULL;
    struct stat fst;

    fpr = fopen(pSrc, "r");
    if(fpr == NULL){
    ALOGD("open %s failed. reason = %s", pSrc, strerror(errno));
    ret = -1;
    goto R;
    }

    fpw = fopen(pDst, "w");
    if(fpw == NULL) {
    ALOGD("open %s failed. reason = %s", pDst, strerror(errno));
    ret = -1;
    goto R;
    }

    int fd = fileno(fpr);
    if(fd == -1) {
    ALOGD("file descriptor error. reason = %s", strerror(errno));
    ret = -1;
    goto R;
    }

    fstat(fd, &fst);

    if(fst.st_size > 0)
    {
    buf = malloc(fst.st_size + 1);
    if(buf == NULL)
    {
        ALOGD("malloc failed for read packages.list, reason = %s", strerror(errno));
        ret = -1;
        goto R;
    }
    memset(buf, 0, fst.st_size);
    fread(buf, fst.st_size, 1, fpr);
        fwrite(buf, fst.st_size, 1, fpw);
    free(buf);
    }
    else
    ret = -1;
R:
    if(fpw != NULL) fclose(fpw);
    if(fpr != NULL) fclose(fpr);
    return ret;
}
