/*
 * Copyright (C) 2009 The Android Open Source Project
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

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <cutils/properties.h>
#define LOG_TAG "cmlogd"
#include <cutils/log.h>

#define VER "2.0"
#define DEBUG
#define TOP_LOG_DIR ("/sdcard/logs/offlineLogs")
#define CMLOGD_PID_PROP ("debug.cmlogd.pids")
#define CMLOGD_TID_PROP ("debug.cmlogd.tids")
#define CMLOGD_TIME_PROP ("debug.cmlogd.time")
#define CMLOGD_LOG_SAVE_PROP ("persist.sys.cmlogd.save")
#define CMLOGD_LOG_PATH ("persist.sys.cmlogd.path")
#define CMLOGD_ENABLE_STATE_PROP ("persist.sys.cmlogd.state")
#define CMLOGD_ENABLE_LIST_PROP ("persist.sys.cmlogd.list")
#define FIRSTBOOT_TIME_PROP ("ro.runtime.firstboot")
#define QXDM_MASK_FILE_DIR ("/system/etc/qxdm")

extern int run_command(const char *title, int timeout_seconds, const char *command, ...);
extern pid_t run_command_no_wait(const char *title, const char *command, ...);
extern int create_log_dir(char *path, mode_t mode);
extern void save_prop_info(char* pDir);
extern void save_last_info(char* pDir);
static int read_pid(void);
static int write_pid(void);
static int is_service_running(char *key);

//log function
static int default_stop_logging(int index);

static int start_logcat_logging(int index);
static int stop_logcat_logging(int index);

static int start_qxdm_logging(int index);
static int stop_qxdm_logging(int index);

static int start_tcp_logging(int index);
static int start_kern_logging(int index);
static int stop_kern_logging(int index);

static int start_bt_logging(int index);
static int stop_bt_logging(int index);

static int start_wlan_logging(int index);
static int stop_wlan_logging(int index);

static int start_power_logging(int index);
static int stop_power_logging(int index);

static int start_charge_logging(int index);
static int stop_charge_logging(int index);

static int start_battery_logging(int index);
static int stop_battery_logging(int index);

static int start_sysprof_logging(int index);
static int stop_sysprof_logging(int index);

#define MAX_OP_LEN (32)
#define NAME_LEN (32)
#define MAX_SUB_PID (4)
#define BUILD_TYPE_PROP "ro.build.type"

struct log_info {
    char name[NAME_LEN];
    int enable;
    pid_t pid;
    int (*en_func)(int index); //normal return pid. if enable by setprop, pid=1
    int (*dis_func)(int index);
    char op_par[MAX_OP_LEN];
    pid_t pid_sub[MAX_SUB_PID];
};

enum {
    LOG_MIN = 0,
    LOGCAT_LOG = LOG_MIN,
    MODEM_LOG,
    NET_LOG,
    KERN_LOG,
    BT_LOG,
    GPS_LOG,
    WLAN_LOG,
    POWER_LOG,
    SENSOR_LOG,
    CHARGE_LOG,
    BATT_LOG,
    SYSPROF_LOG,
    LOG_MAX,
};

/*
* main data structure
*/
#define G_BUF_LEN (32)
char g_sub_buf[G_BUF_LEN];
struct log_info info[LOG_MAX+1] = {
    [LOGCAT_LOG] = {
        .name = "logcat",
        .en_func = start_logcat_logging,
        .dis_func = stop_logcat_logging,
    },
    [MODEM_LOG] = {
        .name = "MDMQ",
        .en_func = start_qxdm_logging,
        .dis_func = stop_qxdm_logging,
    },
    [NET_LOG] = {
        .name = "tcpdump",
        .en_func = start_tcp_logging,
        .dis_func = default_stop_logging,
    },
    [KERN_LOG] = {
        .name = "kmsg",
        .en_func = start_kern_logging,
        .dis_func = stop_kern_logging,
    },
    [BT_LOG] = {
        .name = "bluetooth",
        .en_func = start_bt_logging,
        .dis_func = stop_bt_logging,
    },
    [GPS_LOG] = {
        .name = "GPSQ",
        .en_func = start_qxdm_logging,
        .dis_func = stop_qxdm_logging,
    },
    [WLAN_LOG] = {
        .name = "wlan",
        .en_func = start_wlan_logging,
        .dis_func = stop_wlan_logging,
    },
    [POWER_LOG] = {
        .name = "power",
        .en_func = start_power_logging,
        .dis_func = stop_power_logging,
    },
    [SENSOR_LOG] = {
        .name = "SSRQ",
        .en_func = start_qxdm_logging,
        .dis_func = stop_qxdm_logging,
    },
    [CHARGE_LOG] = {
        .name = "charge",
        .en_func = start_charge_logging,
        .dis_func = stop_charge_logging,
    },
    [BATT_LOG] = {
        .name = "battery",
        .en_func = start_battery_logging,
        .dis_func = stop_battery_logging,
    },
    [SYSPROF_LOG] = {
        .name = "sysprof",
        .en_func = start_sysprof_logging,
        .dis_func = stop_sysprof_logging,
    },
    [LOG_MAX] = {
        .name = "max",
    },
};

const char* name_list[LOG_MAX+1] = {
    [LOGCAT_LOG] = "logcat",
    [MODEM_LOG] = "qxdm",
    [NET_LOG] = "tcpdump",
    [KERN_LOG] = "kmsg",
    [BT_LOG] = "bluetooth",
    [GPS_LOG] = "GPSQ",
    [WLAN_LOG] = "wlan",
    [POWER_LOG] = "power",
    [SENSOR_LOG] = "SSRQ",
    [CHARGE_LOG] = "charge",
    [BATT_LOG] = "battery",
    [SYSPROF_LOG] = "sysprof",
    [LOG_MAX] = "max",
};


static bool is_user(void)
{
	char buf[PROPERTY_VALUE_MAX];

	property_get(BUILD_TYPE_PROP, buf, "unknown");
	ALOGD("%s=%s", BUILD_TYPE_PROP, buf);

	if (strncmp(buf, "user", PROPERTY_VALUE_MAX - 1) == 0)
		return true;
	else
		return false;
}

void dump_option_list(void)
{
#ifdef DEBUG
    int i = 0;
    ALOGD("dump info array begin.\n");
    for(i=0; i< LOG_MAX; i++) {
        ALOGD("info[%d].enable=%d.\n",i,info[i].enable);
        if (strlen(info[i].op_par) != 0)
            ALOGD("info[%d].op_par=%s.\n",i,info[i].op_par);
    }
    ALOGD("dump info array end.\n");
#endif
}

void dump_arg(int argc, char **argv)
{
#ifdef DEBUG
    int t;
    for(t=0;t<argc;t++) {
        ALOGD("argv[%d]=%s\n",t,argv[t]);
    }
#endif
}

void dump_pids()
{
#ifdef DEBUG
    int i = 0;
    ALOGD("dump_pids begin.\n");
    for(i=0; i< LOG_MAX+1; i++) {
        ALOGD("%s log pid=%d.\n", info[i].name, info[i].pid);
    }
    ALOGD("g_sub_buf=%s\n", g_sub_buf);
    ALOGD("dump_pids end.\n");
#endif
}

static int write_pid()
{
    char buf[PROPERTY_VALUE_MAX];
    int i = 0, len = 0;

    for (i=0; i<LOG_MAX; i++) {
        len += snprintf(buf+len, PROPERTY_VALUE_MAX-len, "%d:", info[i].pid);
        ALOGD("%s log pid=%d.\n", info[i].name, info[i].pid);
    }
    ALOGD("pid len is %d.\n", len);
    property_set(CMLOGD_PID_PROP, buf);

    len = 0;
    len += snprintf(buf+len, PROPERTY_VALUE_MAX-len, "%d:", getpid());
    len += snprintf(buf+len, PROPERTY_VALUE_MAX-len, "%d:", info[KERN_LOG].pid_sub[0]);
    len += snprintf(buf+len, PROPERTY_VALUE_MAX-len, "%d:", info[KERN_LOG].pid_sub[1]);
    len += snprintf(buf+len, PROPERTY_VALUE_MAX-len, "%d:", info[LOGCAT_LOG].pid_sub[0]);
    len += snprintf(buf+len, PROPERTY_VALUE_MAX-len, "%d:", info[LOGCAT_LOG].pid_sub[1]);
    len += snprintf(buf+len, PROPERTY_VALUE_MAX-len, "%d:", info[LOGCAT_LOG].pid_sub[2]);
    len += snprintf(buf+len, PROPERTY_VALUE_MAX-len, "%d:", info[LOGCAT_LOG].pid_sub[3]);
    len += snprintf(buf+len, PROPERTY_VALUE_MAX-len, "%d:", info[WLAN_LOG].pid_sub[0]);
    len += snprintf(buf+len, PROPERTY_VALUE_MAX-len, "%d:", info[WLAN_LOG].pid_sub[1]);
    ALOGD("tid len is %d.\n", len);
    property_set(CMLOGD_TID_PROP, buf);
    property_set(CMLOGD_TIME_PROP, g_sub_buf);
    return 0;
}

static int read_pid(void)
{
    char *tok = NULL;
    int ret = -1,i=0;
    char tmp_buf[PROPERTY_VALUE_MAX];
    char buf[300];

    ret = property_get(CMLOGD_PID_PROP, buf, NULL);
    if ((ret <=0) || (strcmp(buf, "0") == 0))
        return -1;
    ALOGD("read pid = %s\n", buf);

    ret = property_get(CMLOGD_TID_PROP, tmp_buf, NULL);
    if ((ret <=0) || (strcmp(tmp_buf, "0") == 0))
        return -1;
    ALOGD("read tid = %s\n", tmp_buf);
    strcat(buf, tmp_buf);

    ret = property_get(CMLOGD_TIME_PROP, tmp_buf, NULL);
    if ((ret <=0) || (strcmp(tmp_buf, "0") == 0))
        return -1;
    ALOGD("read time = %s\n", tmp_buf);
	strcat(buf, tmp_buf);
    ALOGD("buf is = %s\n", buf);

    tok = strtok(buf, ":");
    while(tok) {
        info[i++].pid = atoi(tok);
        tok = strtok(NULL, ":");
        if (i > LOG_MAX) {
            info[KERN_LOG].pid_sub[0] = atoi(tok);
            tok = strtok(NULL, ":");
            info[KERN_LOG].pid_sub[1]  = atoi(tok);
			tok = strtok(NULL, ":");
            info[LOGCAT_LOG].pid_sub[0]  = atoi(tok);
			tok = strtok(NULL, ":");
            info[LOGCAT_LOG].pid_sub[1]  = atoi(tok);
			tok = strtok(NULL, ":");
            info[LOGCAT_LOG].pid_sub[2]  = atoi(tok);
			tok = strtok(NULL, ":");
            info[LOGCAT_LOG].pid_sub[3]  = atoi(tok);
			tok = strtok(NULL, ":");
            info[WLAN_LOG].pid_sub[0]  = atoi(tok);
			tok = strtok(NULL, ":");
            info[WLAN_LOG].pid_sub[1]  = atoi(tok);
            break;
        }
    }

    tok = strtok(NULL, ":");
    snprintf(g_sub_buf, G_BUF_LEN, tok, strlen(tok));
    property_set(CMLOGD_PID_PROP, "0");

    dump_pids();
    return 0;
}

static int get_conf_new(void)
{
    char buf[PROPERTY_VALUE_MAX];
    const char *delim = ";";
    int i=0, name_len,cnt=0,tmp=0;
    char *tok = NULL;
    int ret = 0;
    unsigned int mask = 0;

    ret = property_get(CMLOGD_ENABLE_LIST_PROP, buf, "1:logcat;1:kmsg;1:charge");
    if (ret <= 0) {
        ALOGE("%s is not set.\n", CMLOGD_ENABLE_LIST_PROP);
        goto out;
    }

    ALOGD("new enable str=%s.\n",buf);
    // ignore the first flg, it's to global switch
    i = 0;
    tok = strtok(buf, ";");

    while(tok)
    {
    if ((tok[0] == '0') || (tok[0] == '1'))
    {
        if ((strlen(tok) > 2) && (tok[1] == ':'))
        {
        name_len = strlen(tok) - 2;
        for(i = 0 ; i < LOG_MAX ; i++)
        {
            if(strcmp(&tok[2], name_list[i]) == 0)
            break;
        }
        if(i < LOG_MAX)
        {
            info[i].enable = tok[0] - '0';
            cnt = info[i].enable ? cnt + 1 : cnt;
        }
        }
    }
    else if (tok[0] > '1' && tok[0] < '8')
    {
        tmp = tok[0]-'0';
        if(tmp & 0x1){
            info[MODEM_LOG].enable = 1;
            cnt = cnt + 1;}
        if(tmp & 0x2){
            info[GPS_LOG].enable = 1;
            cnt = cnt + 1;}
        if(tmp & 0x4){
            info[SENSOR_LOG].enable = 1;
            cnt = cnt + 1;}
    }
    else
    {
        ALOGD("wrong format option tok=%s.\n",tok);
        goto out;
    }
    tok = strtok(NULL, delim);
    }
//add some sepcial disposal for feature GPS+modem
    mask = info[MODEM_LOG].enable << 2 | info[GPS_LOG].enable << 1 | info[SENSOR_LOG].enable;
    ALOGD("cnt is =%d.\n",cnt);
    switch(mask)
    {
    case 7:
    {
        info[GPS_LOG].enable = 0;
        info[SENSOR_LOG].enable = 0;
        strcpy(info[MODEM_LOG].op_par, "modem+gps+sensor.cfg");
        cnt -= 2;
        break;
    }
    case 6:
    {
        info[GPS_LOG].enable = 0;
        strcpy(info[MODEM_LOG].op_par, "modem+gps.cfg");
        cnt -= 1;
        break;
    }
    case 5:
    {
        info[SENSOR_LOG].enable = 0;
        strcpy(info[MODEM_LOG].op_par, "modem+sensor.cfg");
        cnt -= 1;
        break;
    }
    case 3:
    {
        info[GPS_LOG].enable = 0;
        info[SENSOR_LOG].enable = 0;
        info[MODEM_LOG].enable = 1;
        strcpy(info[MODEM_LOG].op_par, "gps+sensor.cfg");
        cnt -= 1;
        break;
    }
    case 4:
        strcpy(info[MODEM_LOG].op_par, "modem.cfg");
        break;
    case 2:
        strcpy(info[GPS_LOG].op_par, "gps.cfg");
        break;
    case 1:
        strcpy(info[SENSOR_LOG].op_par, "sensor.cfg");
        break;
    default:
        break;
    }

    ret = info[LOG_MAX].enable = cnt;
    dump_option_list();

out:
    ALOGD("return cnt is =%d.\n",cnt);
    return ret;
}

static int get_conf(void)
{
    int try_cnt = 3;
    int ret;

   /* sometimes, the setting sequence of persist.sys.cmlogd
    * and persist.sys.lelogd.list are out of order.
    */
    while(try_cnt) {
        ret = get_conf_new();
        if (ret > 0)
            break;
        try_cnt --;
        sleep(1);
    }
    return ret;
}

static int is_service_running(char *key)
{
    int ret = -1;
    char prop_buf[PROPERTY_VALUE_MAX];
    property_get(key,prop_buf,"0");

    if (strncmp("running", prop_buf, strlen("running")) == 0) {
        ret = 1;
    } else {
        ret = 0;
    }
    return ret;
}

/*
* -1, error
* >0, length of path
*/
static int create_sub_log_dir(char *buf, int index)
{
    int ret = -1;

    if ((index < LOG_MIN) || (index > LOG_MAX))
        return -1;

    ret = snprintf(buf, PATH_MAX, "%s/%s/%s", TOP_LOG_DIR, g_sub_buf, info[index].name);
    if (mkdir(buf, 0666) == 0){
        ALOGD("create_sub_log_dir = %s success\n", buf);
        return ret;}
    else{
        ALOGE("create_sub_log_dir = %s failed with error %s\n", buf,strerror(errno));
        return -1;}
}

static int default_stop_logging(int index)
{
    pid_t pid = info[index].pid;
    int ret = kill(pid, SIGTERM);
    if (ret < 0) {
        ALOGE("kill log service error = %s\n", strerror(errno));
        ret = kill(pid, SIGKILL);
    }

	for(int i=0;i<MAX_SUB_PID;i++)
	{
		if(info[index].pid_sub[i]>0)
		{
			ret = kill(pid, SIGTERM);
	    		if (ret < 0) {
        			ALOGE("kill log service error = %s\n", strerror(errno));
        			ret = kill(pid, SIGKILL);
    			}
		}
	}
    return ret;
}

static int start_logcat_logging(int index)
{
    int ret;
    pid_t pid = 0;
    char path[PATH_MAX];

    ret = create_sub_log_dir(path, index);
    if (ret < 0)
        return ret;
    ALOGD("logcat dir=%s.\n", path);

    //start logcat, 100M x 4
    snprintf(path+ret, PATH_MAX, "/%s", "logcat_main.log");
    pid = run_command_no_wait(LOG_TAG, "logcat", "-f", path,
            "-b", "main", "-v", "threadtime", "-r", "20480", "-n", "10", "*:v", NULL);

    snprintf(path+ret, PATH_MAX, "/%s", "logcat_radio.log");
    info[index].pid_sub[0] = run_command_no_wait(LOG_TAG, "logcat", "-f", path,
            "-b", "radio", "-v", "threadtime", "-r", "20480", "-n", "10", "*:v", NULL);

    snprintf(path+ret, PATH_MAX, "/%s", "logcat_events.log");
    info[index].pid_sub[1] = run_command_no_wait(LOG_TAG, "logcat", "-f", path,
            "-b", "events", "-v", "threadtime", "-r", "20480", "-n", "10", "*:v", NULL);

    snprintf(path+ret, PATH_MAX, "/%s", "logcat_system.log");
    info[index].pid_sub[2] = run_command_no_wait(LOG_TAG, "logcat", "-f", path,
            "-b", "system", "-v", "threadtime", "-r", "20480", "-n", "10", "*:v", NULL);

    snprintf(path+ret, PATH_MAX, "/%s", "logcat_crash.log");
    info[index].pid_sub[3] = run_command_no_wait(LOG_TAG, "logcat", "-f", path,
            "-b", "crash", "-v", "threadtime", "-r", "20480", "-n", "10", "*:v", NULL);
    return pid;
}

static int stop_logcat_logging(int index)
{
#ifndef LE_STABLE_SW
    char path[PATH_MAX];
    snprintf(path, PATH_MAX, "%s/%s/%s", TOP_LOG_DIR, g_sub_buf, info[index].name);
    run_command(NULL, 180, "cp", "-f", "/data/system/packages.list", path, NULL);
    run_command(NULL, 180, "cp", "-f", "/data/system/packages.xml", path, NULL);
#endif
    return default_stop_logging(index);
}


static int start_tcp_logging(int index)
{
    char path[PATH_MAX];
    int ret;
    pid_t pid = 0;

    ret = create_sub_log_dir(path, index);
    if (ret < 0)
        return ret;
    ALOGD("tcp dir=%s.\n", path);

    //start tcpdump, 100*1000000 x 4
    snprintf(path+ret, PATH_MAX, "/%s", "tcp.log");
    pid = run_command_no_wait(LOG_TAG, "tcpdump", "-i", "any", "-nnXSs", "96",
                        "-C", "100", "-W", "4", "-w", path, NULL);

    return pid;
}

static int start_kern_logging(int index)
{
    char path[PATH_MAX];
    int ret;
    pid_t pid = 0;

    ret = create_sub_log_dir(path, index);
    if (ret < 0)
        return ret;
    ALOGD("kern dir=%s.\n", path);

    snprintf(path+ret, PATH_MAX, "/%s", "kern.log");

    system("echo 10 > /proc/sys/kernel/printk");
    pid = run_command_no_wait(LOG_TAG, "logcat", "-f", path,
            "-b", "kernel", "-v", "threadtime", "-r", "20480", "-n", "10", "*:v", NULL);

    info[index].pid_sub[0] = run_command_no_wait(LOG_TAG, "qlogd_qsee", "-o", path,
                        "-i", "/d/tzdbg/qsee_log", "-n", "4", "-s", "10", NULL);
    info[index].pid_sub[1] = run_command_no_wait(LOG_TAG, "qlogd_tz", "-o", path,
                        "-i", "/d/tzdbg/log", "-n", "4", "-s", "10", NULL);

    ALOGD("start_kern_logging pid=%d  pid_qsee=%d  pid_tz=%d.\n", pid, info[index].pid_sub[0] , info[index].pid_sub[1] );
    return pid;
}


static int stop_kern_logging(int index)
{
    system("echo 6 > /proc/sys/kernel/printk");
    return default_stop_logging(index);
}

static int start_qxdm_logging(int index)
{
    char in_path[PATH_MAX];
    char out_path[PATH_MAX];
    int ret = -1;
    char *rel_path = NULL;

    ret = create_sub_log_dir(out_path, index);
    if (ret < 0)
        return ret;
    ALOGD("qxdm log dir=%s.\n", out_path);

    snprintf(in_path,PATH_MAX,"%s/%s",QXDM_MASK_FILE_DIR,info[index].op_par);
    ret = access(in_path, R_OK);
    if (ret < 0) {
        ALOGD("no valid config file is found. diag_mdlog use default mask.");
	    snprintf(in_path,PATH_MAX,"%s/%s",QXDM_MASK_FILE_DIR,"1.cfg");
    }

    rel_path = in_path;
    ret = run_command_no_wait(LOG_TAG, "diag_mdlog", "-ce", "-s","500", "-n", "4", "-f", rel_path, "-o",out_path,NULL);

    return ret;
}

static int stop_qxdm_logging(int index)
{
    return default_stop_logging(index);
}

static int start_bt_logging(int index)
{
    property_set("bluetooth.trace_level", "true");
    return 1;
}

static int stop_bt_logging(int index)
{
	return default_stop_logging(index);
}

static int start_wlan_logging(int index)
{
    char path[PATH_MAX];
    char prop_val[PROPERTY_VALUE_MAX];
    int ret = -1;
	pid_t pid = 0;

    ret = create_sub_log_dir(path, index);
    if (ret < 0)
        return ret;
    ALOGD("wlan log dir=%s.\n", path);
	snprintf(path,PATH_MAX,"%s/%s",path,"wlan_log.txt");
	ALOGD("wlan log is=%s.\n", path);
	info[index].pid_sub[0]= run_command_no_wait(LOG_TAG, "cnss_diag", "-c",NULL);
    ALOGD("info[index].pid_sub[0] =%d.\n", info[index].pid_sub[0]);
	info[index].pid_sub[1] = run_command_no_wait(LOG_TAG, "iwpriv", "wlan 0 dl_loglevel 0",NULL);
	ALOGD("info[index].pid_sub[1] =%d.\n", info[index].pid_sub[1]);

	pid = run_command_no_wait(LOG_TAG, "logcat", "-f", path,NULL);
	ALOGD("pid is =%d.\n", pid);
    return pid;
}

static int stop_wlan_logging(int index)
{
   return default_stop_logging(index);
}

static int start_power_logging(int index)
{
    char path[PATH_MAX];
    char prop_val[PROPERTY_VALUE_MAX];
    int ret = -1;

    ret = create_sub_log_dir(path, index);
    if (ret > 0) {
        snprintf(prop_val, PROPERTY_VALUE_MAX, "%s:%s", "plog_start", path);
        property_set("ctl.start", prop_val);
    }
    return 1;
}

static int stop_power_logging(int index)
{
    pid_t pid = info[index].pid;

    if (pid == 1) {
        property_set("ctl.start", "plog_stop");
        while(is_service_running("init.svc.plog_start")) {
            sleep(1);
        }
        while(is_service_running("init.svc.plog_stop")) {
            sleep(1);
        }
    }
    return 0;
}

static int start_charge_logging(int index)
{
    int ret = -1;
	ret = run_command_no_wait(LOG_TAG, "charge_logger",NULL);
	ret = run_command_no_wait(LOG_TAG, "msm_tsens_logging_sdcard","1000","360000000",NULL);
    return ret;
}

static int stop_charge_logging(int index)
{   
    pid_t pid = info[index].pid;
    char path[PATH_MAX];
    int ret = -1;

    if (pid <= 0)
        goto out;

    property_set("debug.chg_log.en", "0");
    ret = default_stop_logging(index);

out:
    return ret;
}

static int start_battery_logging(int index)
{
    int ret = -1;
	ret = run_command_no_wait(LOG_TAG, "battd",NULL);
    return ret;
}

static int stop_battery_logging(int index)
{   
    pid_t pid = info[index].pid;
    char path[PATH_MAX];
    int ret = -1;

    if (pid <= 0)
        goto out;

    ret = create_sub_log_dir(path, index);
    if (ret > 0)
        run_command(NULL, 180, "cp", "-rf", "/sdcard/battd/", path, NULL);

	ret = default_stop_logging(index);
out:
    return ret;
}


static int start_sysprof_logging(int index)
{
    char path[PATH_MAX];
    char prop_val[PROPERTY_VALUE_MAX];
    int ret = -1;
	pid_t pid = 0;

    ret = create_sub_log_dir(path, index);
    if (ret < 0)
		return ret;
	pid = run_command_no_wait(LOG_TAG, "qlogd_prof", "-o", path,"-n", "4", "-s", "10", NULL);
	return pid;
}

static int stop_sysprof_logging(int index)
{
	return default_stop_logging(index);
}

static void stop_cmlogd()
{
    struct log_info *p = NULL;
    int i,ret=0;
    char buf[PROPERTY_VALUE_MAX];
    char path[PATH_MAX];

    ret = read_pid();
    if (ret < 0)
        return;

    //stop module
    for(i=0; i<LOG_MAX; i++) {
        p = &info[i];
        if (p->pid > 0)
            ret = p->dis_func(i);
        if (ret < 0)
            ALOGE("stop log fail i=%d pid = %d\n", i, p->pid);
    }

    //stop lelogd_start
    p = &info[LOG_MAX];
    if(p->pid > 0)
        default_stop_logging(LOG_MAX);

    sleep(1);
    return;
}

static int start_cmlogd()
{
    struct log_info *p = NULL;
    struct timeval tv;
    time_t curtime;
    struct tm *tm_ptr = NULL;
    pid_t pid;
    char path[PATH_MAX];
    char *buffer = g_sub_buf;
    int need_cmlogd_alive = 0;
    int i=0, ret = -1,try_cnt=20;

    //get config from config file
    ret = get_conf();
    if ((ret < 0) || (info[LOG_MAX].enable == 0)) {
        ALOGD("no mask for any log. exit...");
        goto out;
    }
try_again:
    // create TOP_LOG_DIR if doesn't exist
    ret = create_log_dir(TOP_LOG_DIR, 0666);
    if(errno != EEXIST && (ret < 0)){
        ALOGE("create dir %s failed with error %s, will try %d times later",TOP_LOG_DIR,strerror(errno),try_cnt);
        if(try_cnt-- > 0) {
            sleep(1);
            goto try_again;
        }
        else
            goto out;
    }
    //create directory by time
    gettimeofday(&tv, NULL);
    curtime=tv.tv_sec;
    tm_ptr = localtime(&curtime);
    if (tm_ptr)
        strftime(buffer, 30, "Log_%Y%m%d_%H%M%S", tm_ptr);
    else
        strlcpy(buffer, "Log_00000000_000000", 30);

    snprintf(path, PATH_MAX, "%s/%s", TOP_LOG_DIR, buffer);

    property_set(CMLOGD_LOG_PATH, path);

    ret = create_log_dir(path,0666);
    if (ret < 0){
        ALOGE("create dir %s failed with error %s.",path,strerror(errno));
        goto out;
    }
    save_prop_info(path);
    save_last_info(path);
    chdir(path);

    //start logging
    ALOGD("conf ready. start logging");
    for(i=0; i<LOG_MAX; i++) {
        p = &info[i];
        if (p->enable) {
            ALOGD("start %s logging\n", p->name);
            pid = p->en_func(i);
            if (pid > 0) {
                p->pid = pid;
                need_cmlogd_alive = 1;
            }
        }
    }
    if (need_cmlogd_alive)
         write_pid();

out:
    return need_cmlogd_alive;
}

/*
int main(int argc, char **argv)
{
    int ch = 0;
    int kill_logd = 0;
    int ret = 0;

    dump_arg(argc, argv);
    while ((ch = getopt(argc, argv, "k")) != -1) {
        switch (ch) {
        case 'k':
            kill_logd = 1;
            break;
        }
    }

    //kill cmlogd
    if (kill_logd == 1) {
        ALOGD("kill_logd=%d.\n",kill_logd);
        if (1 == is_service_running(CMLOGD_ENABLE_STATE_PROP)) {
            stop_cmlogd();
            ALOGD("stop_cmlogd done.");
        } else {
            ALOGD("persist.sys.cmlogd.state is not running.");
        }
        property_set(CMLOGD_ENABLE_STATE_PROP, "x");
        return 0;
    }

    //start cmlogd
    ret = start_cmlogd();
    if (ret > 0) {
        property_set(CMLOGD_ENABLE_STATE_PROP, "running");
        ALOGD("capture is ongoing...");
        while(1) {
            sleep(60);
        }
    }

    ALOGD("cmlogd exit...");
    return 0;
}
*/

#include "cmdif.h"

void usage()
{
    printf("[usage]:\n");
    printf("cmdif command_name [command_param1] [command_param2] ...\n");
    printf("\n");
}

int read_command(IN char *cmd, IN int cmd_size)
{
    int i=0, name_len,cnt=0,tmp=0;
    int ret = 0;
    unsigned int mask = 0;

    ret = property_get(CMLOGD_ENABLE_LIST_PROP, cmd, "");
    if (ret <= 0) 
    {
        ALOGE("%s is not set.\n", CMLOGD_ENABLE_LIST_PROP);
        return -1;
    }
    
    return 0;
}

int exec_command(IN char *cmd, OUT char *output, IN int output_size)
{
    const int MAX_LINE_BUF_LEN = 16384;
    char line[MAX_LINE_BUF_LEN];
    FILE *fp = NULL;
    
    fp = popen(cmd, "r");   
    if (fp == NULL)
    {
        sprintf(output, "popen cmd failed, error = %d\n", errno);
        return -1;
    }
    
    memset(line, 0, MAX_LINE_BUF_LEN);
    while(fgets(line, sizeof(line), fp) != NULL)
    {
        ALOGD("***line=\"%s\"\n", line);
        strcat(output, line);
    }
     
    pclose(fp); 
    return 0;
}

int main(int argc, char **argv)
{
    const int MAX_CMD_LEN = 1024;
    const int MAX_OUTPUT_BUF_SIZE = 1024000;
    char cmd[MAX_CMD_LEN];
    char output[MAX_OUTPUT_BUF_SIZE];
    int i = 0;
    
    memset(cmd, 0, sizeof(cmd));
    memset(output, 0, sizeof(output));
    
    /*
    if (argc < 2) 
    {
        usage();
        return -1;
    }

    for (i=1; i<argc; i++) 
    {
        strcat(cmd, argv[i]);
        if (i != (argc - 1))
            strcat(cmd, " ");
    }
    */
    
    if (read_command(cmd, sizeof(cmd)) < 0)
    {
        sprintf(output, "read command failed, error = %d\n", errno);
    }
    else
    {    
        ALOGD("***cmd=\"%s\"\n", cmd);
        exec_command(cmd, output, sizeof(output));
    }    
    
    ALOGD("***output:\n");
    ALOGD("----------------\n");
    ALOGD("%s\n", output);
    ALOGD("----------------\n");
    return 0;
}
