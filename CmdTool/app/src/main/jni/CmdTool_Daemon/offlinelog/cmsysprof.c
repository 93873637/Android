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

#define BUF_LEN (8*1024)
#define DEFAULT_OUT_DIR ("/sdcard/logs/offlineLogs")
#define DEFAULT_INPUT "/dev/kmsg"
#define DEFAULT_MEMINFO_PATH "/proc/meminfo"
#define DEFAULT_STATINFO_PATH "/proc/stat"
#define STAT_LOG_NAME "stat_info"
#define MEM_LOG_NAME "mem_info"

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

	len = snprintf(buf, PATH_MAX, "%s/sysprof/", DEFAULT_OUT_DIR);
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
	char *out_path = NULL, *in_path = NULL, *meminfo_path = NULL, *statinfo_path = NULL;
	int num=0, size=0, curr_size_stat=0, curr_size_mem=0,stat_full=0,mem_full=0;
	int curr_no_stat = 0;
	int curr_no_mem = 0;
	char tmp_stat[PATH_MAX];
	char tmp_mem[PATH_MAX];
	char read_buf_stat[BUF_LEN]={0};
	char read_buf_mem[BUF_LEN]={0};
	int in_stat_fd, out_stat_fd, in_mem_fd, out_mem_fd;
	ssize_t read_stat_len, write_stat_len, read_mem_len, write_mem_len;
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
	ALOGE("out_path =%s", out_path);

	if(create_log_dir(out_path, 0666))
		if(errno != EEXIST){
			ALOGE("create dir %s failed with error %s.\n",out_path,strerror(errno));
			return -1;
		}

	if (in_path == NULL)
		in_path = DEFAULT_INPUT;

	meminfo_path = DEFAULT_MEMINFO_PATH;
	statinfo_path = DEFAULT_STATINFO_PATH;

	if (num == 0)
		num = 1;
	if (size == 0)
		size = 10;

	if ((size <= 0) || (size > 100) || (num < 0) || (num > 10)) {
		ALOGE("out of scope size=%d num=%d", size, num);
		return -1;
	}

	size = 1024 * 1024 * size;

	curr_no_stat = 0;
	curr_no_mem = 0;

	stat_full=0;
	snprintf(tmp_stat, PATH_MAX, "%s/%s.%d", out_path, STAT_LOG_NAME, curr_no_stat);
	ALOGD("curr stat log file path=%s", tmp_stat);
	out_stat_fd = open(tmp_stat, O_WRONLY | O_CREAT | O_TRUNC, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
	if (out_stat_fd < 0) {
		ALOGE("open stat info file fail error = %s\n", strerror(errno));
		return -1;
	}

	mem_full=0;
	snprintf(tmp_mem, PATH_MAX, "%s/%s.%d", out_path, MEM_LOG_NAME, curr_no_mem);
	ALOGD("curr mem log file path=%s", tmp_mem);
	out_mem_fd = open(tmp_mem, O_WRONLY | O_CREAT | O_TRUNC, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
	if (out_mem_fd < 0) {
		ALOGE("open stat info file fail error = %s\n", strerror(errno));
		return -1;
	}

	while(1) {
		if(stat_full == 1){
			stat_full=0;
			snprintf(tmp_stat, PATH_MAX, "%s/%s.%d", out_path, STAT_LOG_NAME, curr_no_stat);
			ALOGD("curr stat log file path=%s", tmp_stat);
			out_stat_fd = open(tmp_stat, O_WRONLY | O_CREAT | O_TRUNC, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
			if (out_stat_fd < 0) {
				ALOGE("open mem info file fail error = %s\n", strerror(errno));
				return -1;
			}
			curr_size_stat = 0;
		}
		if(mem_full == 1){
			mem_full=0;
			snprintf(tmp_mem, PATH_MAX, "%s/%s.%d", out_path, MEM_LOG_NAME, curr_no_mem);
			ALOGD("curr mem log file path=%s", tmp_mem);
			out_mem_fd = open(tmp_mem, O_WRONLY | O_CREAT | O_TRUNC, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
			if (out_mem_fd < 0) {
				ALOGE("open stat info file fail error = %s\n", strerror(errno));
				return -1;
			}
			curr_size_mem = 0;
		}

		while(1)
		{
			/* main loop */
			in_stat_fd = open(statinfo_path, O_NONBLOCK,  S_IRUSR | S_IRGRP | S_IROTH);
			if (in_stat_fd < 0) {
				ALOGE("open statinfo %s fail error = %s\n", statinfo_path, strerror(errno));
				return -1;
			}

			in_mem_fd = open(meminfo_path, O_NONBLOCK,  S_IRUSR | S_IRGRP | S_IROTH);
			if (in_mem_fd < 0) {
				ALOGE("open meminfo %s fail error = %s\n", meminfo_path, strerror(errno));
				return -1;
			}

			read_stat_len = read(in_stat_fd, read_buf_stat, BUF_LEN);
			//ALOGE(" in_stat_fd=0x%x  out_stat_fd=0x%x   read_buf_stat=0x%x read_stat_len=%d\r\n", in_stat_fd, out_stat_fd, read_buf_stat, read_stat_len);
			//ALOGE(" read_buf_tz=%s \r\n ", read_buf_stat);
			if (read_stat_len > 0) {
				write_stat_len = write(out_stat_fd, read_buf_stat, read_stat_len);
				fsync(out_stat_fd);
				curr_size_stat += write_stat_len;
				if (curr_size_stat > size) {
					fsync(out_stat_fd);
					close(out_stat_fd);
					stat_full = 1;
					if(curr_no_stat++>num)
						curr_no_stat=0;
				}
			} 
			close(in_stat_fd);

			read_mem_len = read(in_mem_fd, read_buf_mem, BUF_LEN);
			//ALOGE(" in_mem_fd=0x%x  out_mem_fd=0x%x   read_buf_mem=0x%x read_mem_len=%d\r\n", in_mem_fd, out_mem_fd, read_buf_mem, read_mem_len);
			//ALOGE(" read_buf_mem=%s \r\n ", read_buf_mem);
			if (read_mem_len > 0) {
				write_mem_len = write(out_mem_fd, read_buf_mem, read_stat_len);
				fsync(out_mem_fd);
				curr_size_mem += write_mem_len;
				if (curr_size_mem > size) {
					fsync(out_mem_fd);
					close(out_mem_fd);
					mem_full = 1;
					if(curr_no_mem++>num)
						curr_no_mem=0;
				}
			}
			close(in_mem_fd);
			sleep(5);
			if(stat_full || mem_full)
				break;
		}
	}
	ALOGE("cmsysprof should never run here!\n");
	return 0;
}

