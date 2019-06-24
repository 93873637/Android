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

#define BUF_LEN (8192)
#define DEFAULT_OUT_DIR ("/sdcard/logs/offlineLogs")
#define DEFAULT_INPUT "/dev/kmsg"

extern int create_log_dir(char *path, mode_t mode);

#if 0
void flush_buffer(int signal)
{
	fsync();
}
#endif

static void usage() {
    fprintf(stderr, "usage: cmkmsg -o path -n file_num -s size\n"
					"-i, input log path, only absolute path supported\n"
					"-o, log path, only absolute path supported\n"
					"-n, file_num, the max file nummber\n"
					"-s, each log file size\n");
}

static char *get_def_path(void)
{
	char *buf = calloc(1, PATH_MAX);
	int len = 0, ret = 0;
    struct timeval tv;
    time_t curtime;
    struct tm *tm_ptr = NULL;

	len = snprintf(buf, PATH_MAX, "%s/dmesg/", DEFAULT_OUT_DIR);
	ret = create_log_dir(buf,0666);
    if (-1 == ret) {
        ALOGD("func=%s line=%d.\n",__FUNCTION__,__LINE__);
        free(buf);
		return NULL;
    }

	gettimeofday(&tv, NULL);
    curtime=tv.tv_sec;
    tm_ptr = localtime(&curtime);
    strftime(buf+len, 30, "Log_%Y%m%d_%H%M%S", tm_ptr);

	return buf;
}

int main(int argc, char **argv)
{
	char *exe;
	exe = argv[0];
	int c;
	char *out_path = NULL, *in_path = NULL;
	int num=0, size=0, curr_size=0;
	int curr_no = 0;
	char tmp[PATH_MAX];
	char read_buf[BUF_LEN];
	char *outfile;
	struct stat file_stat;
	int in_fd, out_fd;
	ssize_t read_len, write_len;
	unsigned int time_left = 0;

	while ((c = getopt(argc, argv, "ho:n:s:i:")) != -1) {
		switch (c) {
		case 'i':
			in_path = optarg;
			break;
		case 'o':
			out_path= optarg;
			break;
		case 'n':
			num = atoi(optarg);
			break;
		case 's':
			size= atoi(optarg);
			break;
		case 'h':
		  usage();
		  return 0;
		}
	}

	if (out_path == NULL) {
		out_path = get_def_path();
		if (out_path == NULL)
			return -1;
	}

	if (out_path[0] != '/'){
		ALOGE("out_path =%s", out_path);
		return -1;
	}

	if (in_path == NULL)
		in_path = DEFAULT_INPUT;
	if (num == 0)
		num = 1;
	if (size == 0)
		size = 10;

	if ((size <= 0) || (size > 100) || (num < 0) || (num > 10)) {
		ALOGE("out of scope size=%d num=%d", size, num);
		return -1;
	}
	
	size = 1024 * 1024 * size;
#if 0
	sigemptyset( &sact.sa_mask );
	sact.sa_flags = 0;
	sact.sa_handler = flush_buffer;
	sigaction(SIGTERM, &sact, NULL);
	sigaction(SIGHUP, &sact, NULL);
	sigaction(SIGUSR1, &sact, NULL);
	sigaction(SIGINT, &sact, NULL);
#endif

	/* main loop */
	in_fd = open(in_path, O_NONBLOCK,  S_IRUSR | S_IRGRP | S_IROTH);
	if (in_fd < 0) {
		ALOGE("open %s fail error = %s\n", in_path, strerror(errno));
		sleep(1000);
		return -1;
	}

	curr_no = 0;
	while(1) {
		snprintf(tmp, PATH_MAX, "%s.%d", out_path, curr_no);
		ALOGD("curr kmsg log file path=%s", tmp);

		out_fd = open(tmp, O_WRONLY | O_CREAT | O_TRUNC, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);

	    if (out_fd < 0) {
	        ALOGE("open file fail error = %s\n", strerror(errno));
	        return -1;
	    }

		curr_size = 0;
	  while(1) {
			read_len = read(in_fd, read_buf, BUF_LEN);
	        if (read_len > 0) {
	            write_len = write(out_fd, read_buf, read_len);
				if (time_left > 0)
					fsync(out_fd);

				curr_size += write_len;
				if (curr_size > size) {
					fsync(out_fd);
					break;
				}
	        } else {
				time_left = sleep(1);
	        }
	    }

		if (curr_no++ > num)
			curr_no = 0;
	}

	close(out_fd);
	return 0;
}
