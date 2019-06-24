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
#include <sys/ioctl.h>
#include <linux/fs.h>

#include <cutils/properties.h>

#include <cutils/log.h>
#include <selinux/selinux.h>

#include "rdump_qc.h"

#ifdef LOG_TAG
#undef LOG_TAG
#define LOG_TAG "rdumpext"
#endif

#define TIMEBUF_SIZE 21
#define RAMDUMP_PARTI_PATH ("/dev/block/sda")
#define RAMDUMP_PARTI_SE ("u:object_r:le_rdump_device:s0")
#define MAX_SDA_NUM (20)
#define RAMDUMP_ADDI_SIZE (100*1024*1024ULL)
#define TOP_DUMP_DIR ("/sdcard/logs/ramdump/")
#define HEADER_BUF_LEN (8192)
#define BUFF_SIZE (4096)
#define FILENAME_SIZE 512
#define RAMDUMP_SAV_FLG ("persist.sys.ramdump_sav_flg")

int get_rdump_parti_fd(void)
{
	int fd = -1;
	char buf[1024];
	int i = 0, len = 0, ret;
	char *maclabel = NULL;
	int rdump_parti_founded = 0;
	uint64 size, ramsize;

	for (i = 1; i < MAX_SDA_NUM; i++) {
		len = sprintf(buf, "%s%d", RAMDUMP_PARTI_PATH, i);
		buf[len] = 0;

		lgetfilecon(buf, &maclabel);
		if (!maclabel) {
			continue;
		}

		ALOGD("maclabel %d is  %s\n", i,maclabel);

		if(!strcmp(maclabel, RAMDUMP_PARTI_SE))
			rdump_parti_founded = 1;

		free(maclabel);
		maclabel = NULL;

		if (rdump_parti_founded)
			break;
	}

	if (rdump_parti_founded == 0)
		return -1;

	return i;
}

int blk_rw(int fd, int rw, int64_t offset, uint8_t *buf, unsigned len)
{
    int r;

    if (lseek(fd, offset, SEEK_SET) < 0) {
        ALOGE("block dev lseek64 %ld failed: %s\n", offset,
                strerror(errno));
        return -1;
    }

    if (rw)
        r = write(fd, buf, len);
    else
        r = read(fd, buf, len);

    if (r < 0)
        ALOGE( "block dev %s failed: %s\n", rw ? "write" : "read",
                strerror(errno));
    else
        r = 0;

    return r;
}

static int create_log_dir(char* pDir, mode_t mode) {
    int i = 0;
    int iRet = 0;
    int iLen;
    char* pszDir;

    if (NULL == pDir) {
        return -1;
    }

    pszDir = strdup(pDir);
    iLen = strlen(pszDir);
    ALOGD("pszDir %s\n", pszDir);
    // create dir
    for (i = 0; i < iLen+1; i++) {
        if ((pszDir[i] == '/' && i != 0) || pszDir[i] == '\0') {
            pszDir[i] = '\0';
            ALOGI("pathDir1 %s\n", pszDir);
            //if dir not exit, create
            iRet = access(pszDir, 0);
            if (iRet != 0) {
                iRet = mkdir(pszDir, mode);
                if (iRet != 0) {
                    free(pszDir);
                    return -1;
                }
            }
            pszDir[i] = '/';
        }
    }
    free(pszDir);
    return iRet;
}


char *get_current_timestamp(char *buf, int len)
{
	time_t local_time;
	struct tm *tm;

	if (buf == NULL || len < TIMEBUF_SIZE) {
	ALOGE("Invalid timestamp buffer");
	goto get_timestamp_error;
	}

	/* Get current time */
	local_time = time(NULL);
	if (!local_time) {
	ALOGE("Unable to get timestamp");
	goto get_timestamp_error;
	}

	tm = localtime(&local_time);
	if (!tm) {
	ALOGE("Unable to get local time");
	goto get_timestamp_error;
	}

	snprintf(buf, TIMEBUF_SIZE,
		"%04d-%02d-%02d_%02d-%02d-%02d", tm->tm_year+1900,
		tm->tm_mon+1, tm->tm_mday, tm->tm_hour, tm->tm_min,
		tm->tm_sec);

	return buf;

get_timestamp_error:
	return NULL;
}

extern int run_command(const char *title, int timeout_seconds, const char *command, ...);

static char header_buf[HEADER_BUF_LEN] __attribute__((aligned(HEADER_BUF_LEN)));

int main(int argc, char **argv)
{
	int parti_num = -1;
	int fdr = 0;
	FILE* fdw = NULL;
	int ret = 0;
	int len=0;
	uint64_t left_size;
	char tmp_buf[256];
	char timestamp[TIMEBUF_SIZE];
	char *buf_dump;
	uint32 i;

	static char dump_name_buf_tmp[FILENAME_SIZE];
	static char cmd_buf[FILENAME_SIZE];
	static char dump_name_buf[FILENAME_SIZE];
	struct boot_raw_parition_dump_header *phdump = NULL;
	struct boot_raw_partition_dump_section_header *phsec = NULL;

	phdump = (struct boot_raw_parition_dump_header *)malloc(sizeof(struct boot_raw_parition_dump_header));
	phsec = (struct boot_raw_partition_dump_section_header *)malloc(sizeof(struct boot_raw_partition_dump_section_header));
	buf_dump = (char*)malloc(BUFF_SIZE);

	parti_num = get_rdump_parti_fd();
	if (parti_num < 0) {
		property_set(RAMDUMP_SAV_FLG, "3");
		ALOGE("Find parti num failed.\n");
		ret = -EINVAL;
		goto exit;
	}
	memset(tmp_buf,0,sizeof(tmp_buf));
	snprintf(tmp_buf, sizeof(tmp_buf),"%s%d",RAMDUMP_PARTI_PATH,parti_num);
	ALOGD("open partition %s .\n",tmp_buf);

	fdr = open(tmp_buf, O_RDWR);
	if (fdr< 0) {
		ALOGE("open %s with error %s.\n",tmp_buf, strerror(errno));
		ret=-EINVAL;
		goto exit;
	}

	if(!blk_rw(fdr, 0, 0, (uint8*)phdump, sizeof(struct boot_raw_parition_dump_header)))
		ALOGD("section count is %d .\n",phdump->sections_count);
	else{
		ALOGE("open %s with error %s.\n",tmp_buf, strerror(errno));
		ret=-EINVAL;
		goto exit;
	}

	if(phdump->validity_flag == 0){
		ALOGD("validity_flag is zero means dump already has been extracted .\n");
		ret=-EINVAL;
		goto exit;
	}

	if (get_current_timestamp(timestamp, TIMEBUF_SIZE) == NULL)
	{
		ALOGE("Unable to get timestamp for log.\n");
		ret = -EINVAL;
		goto exit;
	}

	memset(dump_name_buf,0,sizeof(dump_name_buf_tmp));
	sprintf(dump_name_buf, "%s%s",TOP_DUMP_DIR,timestamp);
	ret = create_log_dir(dump_name_buf,0777);
	if (ret != 0){
		ALOGE("create %s fail error=%s.\n",dump_name_buf_tmp,strerror(errno));
		goto exit;
	}

	for(i=0;i<phdump->sections_count;i++)
	{
		len=blk_rw(fdr, 0, sizeof(struct boot_raw_parition_dump_header)+i*sizeof(struct boot_raw_partition_dump_section_header), (uint8*)phsec, sizeof(struct boot_raw_partition_dump_section_header));
		ALOGD("section name is  %s .\n",(char*)phsec->section_name);
		ALOGD("section section_size is  %llx .\n",phsec->section_size);
		ALOGD("section section_offset is  %llx .\n",phsec->section_offset);

		memset(dump_name_buf_tmp,0,sizeof(dump_name_buf_tmp));
		sprintf(dump_name_buf_tmp, "%s%s%s",dump_name_buf,"/",(char*)phsec->section_name);
		ALOGD("save dump file %s .\n",dump_name_buf_tmp);

		left_size = phsec->section_size;
		uint64_t offset = phsec->section_offset;
		fdw = fopen(dump_name_buf_tmp, "ab");
		while(left_size)
		{
			uint32_t read_size = left_size > BUFF_SIZE ? BUFF_SIZE : left_size;
			len = blk_rw(fdr, 0, offset, (uint8*)buf_dump, read_size);

			if(fdw != NULL)
				fwrite((char*)buf_dump, 1, read_size, fdw);
			else{
				ALOGE("open %s fail error=%s.\n",dump_name_buf_tmp,strerror(errno));
				ret=-EINVAL;
				goto exit;
			}
			offset += read_size;
			left_size =(left_size > read_size) ? (left_size - read_size) : 0;
		}
		fflush(fdw);
		fclose(fdw);
	}

	phdump->validity_flag = 0;//clear valid flg
	if(!blk_rw(fdr, 1, 0, (uint8*)phdump, sizeof(struct boot_raw_parition_dump_header)))
		ALOGD("clear validity_flag success.\n");
	else
		ALOGE("clear validity_flag failed with error=%s.\n",strerror(errno));
	close(fdr);

	memset(dump_name_buf_tmp,0,sizeof(dump_name_buf_tmp));
	memset(cmd_buf,0,sizeof(dump_name_buf_tmp));
	sprintf(dump_name_buf_tmp, "%s%s%s",TOP_DUMP_DIR,timestamp,".tar.gz");
	sprintf(cmd_buf, "tar -zcvf  %s %s",dump_name_buf_tmp,dump_name_buf);
	ALOGD("run compress cmd %s.\n",cmd_buf);
	system(cmd_buf);

	memset(cmd_buf,0,sizeof(dump_name_buf_tmp));
	sprintf(cmd_buf, "rm -rf %s",dump_name_buf);
	system(cmd_buf);
	ALOGD("run delete cmd %s.\n",cmd_buf);
	property_set(RAMDUMP_SAV_FLG, "1");
	ALOGD("Extract and compress dump success.\n");
	free(phdump);
	free(phsec );
	free(buf_dump);
	return ret;
exit:
	free(phdump);
	free(phsec );
	free(buf_dump);
	property_set(RAMDUMP_SAV_FLG, "3");
	return ret;
}
