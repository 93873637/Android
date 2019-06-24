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
#define LOG_TAG "rdumpsvc"
#include <cutils/log.h>
#include <selinux/selinux.h>

#include "rdump_qc.h"

#define RAMDUMP_STATUS_PROP ("ro.debug.ramdump.status")
#define RAMDUMP_REQ_PROP ("persist.sys.save.ramdump")
#define RAMDUMP_ENALBE_FILE ("/sys/kernel/dload/emmc_dload")
#define BUFF_LEN (PROPERTY_VALUE_MAX)

#if 1
/*
* get ramdump partition status
*
* if RAMDUMP_STATUS_PROP is set, read status from it.
* if not, read status from /proc/cmdline and set the result
* to RAMDUMP_STATUS_PROP
*/
int get_ramdump_status(char *buf)
{
	int fd = -1, i;
	char *p = NULL;
	int len;
	int ramdump_prop_avail = 0;
	char *local_buf = NULL;

	len = property_get(RAMDUMP_STATUS_PROP, buf, NULL);
	if (len > 0) {
		ramdump_prop_avail = 1;
		goto exit;
	}

	local_buf = malloc(4096);
	if (local_buf == NULL)
		goto exit;

	//RAMDUMP_STATUS_PROP is not set, set it
	fd= open("/proc/cmdline", O_RDONLY, O_NOATIME|O_NONBLOCK);
	if (fd < 0) goto exit;

	len = read(fd, local_buf, 4096);
	if (len <= 0) goto exit;
	local_buf[len] = 0;

	p = strstr(local_buf, "rdump");
	if (p == NULL) goto exit;

	p += strlen("rdump");
	for (i=0; i<5; i++) {
		char c = *p;
		if ((c >= '0') && (c <= '9')) {
			buf[0] = c;
			buf[1] = 0;
			break;
		}
		p++;
	}
	if (i == 5) goto exit;

	//set property
	property_set(RAMDUMP_STATUS_PROP, buf);
	close(fd);
	return 0;

exit:
	if (ramdump_prop_avail == 0) {
		buf[0] = '0';
		buf[1] = 0;
		property_set(RAMDUMP_STATUS_PROP, buf);
	}
	if (fd > 0)
		close(fd);
	if (local_buf)
		free(local_buf);

	return 0;
}

int update_dload_status(int req_val)
{
	int curr_val = 0;
	int fd = -1;
	char buf[32];
	int len;

	fd= open(RAMDUMP_ENALBE_FILE, O_RDWR, O_NOATIME|O_NONBLOCK);
	if (fd < 0) goto exit;

	len = read(fd, buf, 32);
	if (len <= 0) {
		ALOGE("read errors =%s.\n", strerror(errno));
	}
	curr_val = buf[0] - '0';

	if (curr_val != req_val) {
		buf[0] = req_val + '0';
		buf[1] = 0;
		len = write(fd, buf, strlen(buf));
		if (len <= 0) {
			ALOGE("write errors =%s.\n", strerror(errno));
		}
	}
	close(fd);

exit:
	return 0;
}

/*
* write RAMDUMP_REQ_PROP to RAMDUMP_ENALBE_FILE
*/
int set_main(void)
{
	int req_val = 0, enable =0;
	int len;
	char buf[PROPERTY_VALUE_MAX];

	len = property_get(RAMDUMP_REQ_PROP, buf, NULL);
	if (len <= 0)
		goto exit;

	req_val = buf[0] - '0';
	if ((req_val != 0) && (req_val != 1)) {
		ALOGE("invalid val = %s", buf);
		goto exit;
	}

	return update_dload_status(req_val);

exit:
	return 0;
}

int main(int argc, char **argv)
{
	char buff[BUFF_LEN];

	get_ramdump_status(buff);
	if ((buff[0] == '1') || (buff[0] == '2')) {
		//ramdump partition is available
		set_main();
	}
	return 0;
}
#else
#define RAMDUMP_PARTI_PATH ("/dev/block/sda")
#define RAMDUMP_PARTI_SE ("u:object_r:le_rdump_device:s0")
#define MAX_SDA_NUM (20)
#define RAMDUMP_ADDI_SIZE (100*1024*1024ULL)

uint64 get_phy_ram_size(void)
{
	uint64 ramsize;
}

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

		if(!strcmp(maclabel, RAMDUMP_PARTI_SE))
			rdump_parti_founded = 1;

		free(maclabel);
		maclabel = NULL;

		if (rdump_parti_founded)
			break;
	}

	if (rdump_parti_founded == 0)
		return -1;

	fd= open(buf, O_RDONLY, O_NOATIME|O_NONBLOCK);
	if (fd < 0) {
			ALOGE("open error %d\n", errno);
			return -1;
	}

	ret = ioctl(fd, BLKGETSIZE64, &size);
	if (ret < 0) {
        ALOGE("ioctl error %d\n", errno);
        close(fd);
		return -1;
    }

	ramsize = get_phy_ram_size();
	if (size < (ramsize + RAMDUMP_ADDI_SIZE)) {
		ALOGE("size=0x%xKB, ramsize=0x%xKB\n", size/1024, ramsize/1024);
		close(fd);
		return -1;
	}
	return fd;
}

int is_ramdump_valid (int fd)
{
	char buf[4096];
	int ret;
	unsigned char dump_sig[] = RAM_DUMP_HEADER_SIGNATURE;
	struct boot_raw_parition_dump_header *pdump = (struct boot_raw_parition_dump_header *)buf;

	ret = read(fd, buf, sizeof(struct boot_raw_parition_dump_header));
	if (ret != sizeof(struct boot_raw_parition_dump_header)) {
		return 0;
	}

	if ((pdump->validity_flag == 1)
		&& (0 == memcmp(pdump->signature, dump_sig, sizeof(pdump->signature)))) {
		ret = 1;
	} else {
		ret = 0;
	}

	return ret;
}

int main(int argc, char **argv)
{
	int fd = -1;

	char buf[1024];
	ssize_t len;

	fd= get_rdump_parti_fd();
	if (fd < 0) {
		property_set(RAMDUMP_STATUS_PROP, "0");
		goto exit;
	}

	if (is_ramdump_valid(fd)) {
		property_set(RAMDUMP_STATUS_PROP, "2");
	} else {
		property_set(RAMDUMP_STATUS_PROP, "1");
	}
	close(fd);

exit:
	return 0;
}
#endif
