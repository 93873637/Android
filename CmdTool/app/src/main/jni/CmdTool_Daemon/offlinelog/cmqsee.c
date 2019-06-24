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

#define QSEE_BUF_LEN (64*1024)
#define DEFAULT_OUT_DIR ("/sdcard/logs/offlineLogs")
#define DEFAULT_INPUT "/dev/kmsg"
#define DEFAULT_QSEE_PATH "/d/tzdbg/qsee_log"
#define DEFAULT_TZ_PATH "/d/tzdbg/log"
#define QSEE_LOG_NAME "qsee"
#define TZ_LOG_NAME "tz"

extern int create_log_dir(char *path, mode_t mode);

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
	char *out_path = NULL, *in_path = NULL, *qsee_path = NULL, *tz_path = NULL;
	int num=0, size=0, curr_size=0, curr_size_qsee=0, curr_size_tz=0;
	int curr_no = 0;
	char tmp[PATH_MAX];
	char read_buf_qsee[QSEE_BUF_LEN]={0};
	char *outfile;
	struct stat file_stat;
	int in_fd, out_fd, in_qsee_fd, out_qsee_fd, in_tz_fd, out_tz_fd;
	ssize_t read_len, write_len, read_qsee_len, write_qsee_len, read_tz_len, write_tz_len;
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

	qsee_path = DEFAULT_QSEE_PATH;
	tz_path = DEFAULT_TZ_PATH;

	if (num == 0)
		num = 1;
	if (size == 0)
		size = 10;

	if ((size <= 0) || (size > 100) || (num < 0) || (num > 10)) {
		ALOGE("out of scope size=%d num=%d", size, num);
		return -1;
	}

	size = 1024 * 1024 * size;

	/* main loop */
	in_qsee_fd = open(qsee_path, O_NONBLOCK,  S_IRUSR | S_IRGRP | S_IROTH);
	if (in_qsee_fd < 0) {
		ALOGE("open qsee %s fail error = %s\n", qsee_path, strerror(errno));
		return -1;
	}

	curr_no = 0;
	while(1) {

		snprintf(tmp, PATH_MAX, "%s.%s.%d", out_path, QSEE_LOG_NAME, curr_no);
		ALOGD("curr qsee log file path=%s", tmp);

		out_qsee_fd = open(tmp, O_WRONLY | O_CREAT | O_TRUNC, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
		if (out_qsee_fd < 0) {
			ALOGE("open qsee file fail error = %s\n", strerror(errno));
			return -1;
		}

		curr_size_qsee = 0;
		while(1) {

			read_qsee_len = read(in_qsee_fd, read_buf_qsee, QSEE_BUF_LEN);
			//ALOGE(" in_qsee_fd=0x%x  out_qsee_fd=0x%x   read_buf_qsee=0x%x read_qsee_len=%d\r\n", in_qsee_fd, out_qsee_fd, read_buf_qsee, read_qsee_len);
			//ALOGE(" read_buf_qsee=%s \r\n ", read_buf_qsee);
			if (read_qsee_len > 0) {
				write_qsee_len = write(out_qsee_fd, read_buf_qsee, read_qsee_len);
				if (time_left > 0)
					fsync(out_qsee_fd);

				curr_size_qsee += write_qsee_len;
				if (curr_size_qsee > size) {
					fsync(out_qsee_fd);
					break;
				}
			} else {
				time_left = sleep(1);
			}

		}

		if (curr_no++ > num)
			curr_no = 0;
	}

	close(out_qsee_fd);
	return 0;
}
