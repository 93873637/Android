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
#include <sys/ioctl.h>
#include <time.h>
#include <selinux/selinux.h>
#include <sys/time.h>
#include <cutils/klog.h>

#include "lastsvc.h"
#include "last_shared.h"
#include "last_tz.h"

#define TAG "lastsvc"
#define ERROR(x...) do { KLOG_ERROR(TAG, x); } while (0)
#define NOTICE(x...) do { KLOG_NOTICE(TAG, x); } while (0)
#define INFO(x...) do { KLOG_INFO(TAG, x); } while (0)

#define FILE_LAST_PARTI_NAME ("/dev/block/bootdevice/by-name/kernellog")
#define FILE_PANIC_REASON_PROC ("/proc/lst_pnc_rsn")
#define FILE_LAST_KMSG_PROC ("/proc/last_kmsg")
#define FILE_LAST_TZ_DBG ("/sys/kernel/debug/last_tzdbg/log")
#define FILE_LAST_RPM_DBG ("/sys/kernel/debug/last_rpmdbg/log")

#define PROP_PMIC_RESET "ro.debug.pmic_reset"

static struct last_dbg_addr_info *g_paddr = NULL;
static struct last_dbg_info *g_pinfo = NULL;

extern int create_log_dir(char *path, mode_t mode);

int main_last_kmsg(int fd);
int main_panic_reason(int fd, int f_panic);
int main_last_tzlog();
int main_last_rpmlog();

int handle_new_kern(int fd, int f_panic, struct last_dbg_addr_info *p_addr)
{
	int ret;

	NOTICE("handle_new_kern.\n");
	ret = ioctl(f_panic, GET_PANIC_REASON_ADDR_INFO, p_addr);
	if (ret < 0)
		return ret;

	lseek(fd, LAST_DBG_HEAD_ADDR_OFFSET , SEEK_SET);
	write(fd, p_addr, sizeof(struct last_dbg_addr_info));
	return 0;
}

int main_panic_reason(int fd, int f_panic)
{
	char *pbuf = NULL;
	int buf_len;
	int ret;

	/*
	* for panic reason, the data is not only stored in last_info,
	* but also pon register.
	* so even if there's no valid last_info, we still should
	* call last_panic_reason_init() to get the panic reason
	*/
	if (g_pinfo) {
		buf_len = g_paddr->panic_reason_addr.panic_log_buf_len;
		if (buf_len > LAST_PANIC_LOG_MAX_LEN) {
			return -1;
		}

		pbuf = malloc(buf_len);
		if (pbuf == NULL) {
			ERROR("Out of memory\n");
			return -1;
		}

		//read __log_buf buffer
		lseek(fd, LAST_PANIC_LOG_BUF_OFFSET, SEEK_SET);
		read(fd, pbuf, buf_len);

		ret = ioctl(f_panic, SET_LAST_PANIC_LOG_DATA, pbuf);
		if(ret < 0) {
			ERROR("SET_LAST_PANIC_LOG_DATA fail err=%s.\n", strerror(errno));
			goto exit;
		}

		ret = ioctl(f_panic, SET_LAST_PANIC_LOG_INFO, &g_pinfo->panic_info);
		if(ret < 0) {
			ERROR("SET_LAST_PANIC_LOG_INFO fail err=%s.\n", strerror(errno));
			goto exit;
		}
	}

	ret = ioctl(f_panic, LAST_PANIC_REASON_INIT);
	if(ret < 0){
		ERROR("LAST_PANIC_REASON_INIT fail err=%s.\n", strerror(errno));
	}

exit:
	if (pbuf)
		free(pbuf);
	return ret;
}

int main_last_kmsg(int fd)
{
	int f_lk = 0;
	char *pbuf = NULL;
	int buf_len;
	int ret = 0;

	if (g_pinfo == NULL)
		goto exit;

	f_lk = open(FILE_LAST_KMSG_PROC, O_RDONLY);
	if (f_lk < 0) {
		ERROR("last_kmsg is not avail.\n");
		return -1;
	}

	buf_len = g_paddr->last_kmsg_addr.log_buf_len;
	if (buf_len > LAST_INFO_MAX_LEN) {
		ERROR("panic_log_buf_len = %d. too large", buf_len);
		return -1;
	}

	pbuf = malloc(buf_len);
	if (pbuf == NULL) {
		ERROR("Out of memory\n");
		return -1;
	}

	lseek(fd, LAST_DBG_INFO_OFFSET, SEEK_SET);
	read(fd, pbuf, buf_len);

	ret = ioctl(f_lk, SET_LK_LOG_DATA, pbuf);
	if(ret < 0) {
		ERROR("SET_LK_LOG_DATA fail err=%s.\n", strerror(errno));
		goto exit;
	}

	ret = ioctl(f_lk, SET_LK_LOG_INFO, &g_pinfo->last_kmsg);
	if(ret < 0) {
		ERROR("SET_LK_LOG_INFO fail err=%s.\n", strerror(errno));
		goto exit;
	}

	ret = ioctl(f_lk, SET_LK_PON_POFF_INFO, &g_pinfo->pon_poff);
	if(ret < 0) {
		ERROR("SET_LK_PON_POFF_INFO fail err=%s.\n", strerror(errno));
		goto exit;
	}

	ret = ioctl(f_lk, LK_POST_INIT);
	if(ret < 0){
		ERROR("LK_POST_INIT fail err=%s.\n", strerror(errno));
	}

exit:
	if (pbuf)
		free(pbuf);
	return ret;
}

int main_last_tzlog(int fd)
{
	int f_lk = 0;
	char *pbuf = NULL;
	int buf_len;
	int ret = 0;

	if (g_pinfo == NULL)
		goto exit;

	f_lk = open(FILE_LAST_TZ_DBG, O_RDONLY);
	if (f_lk < 0) {
		ERROR("%s is not avail.\n",FILE_LAST_TZ_DBG);
		return -1;
	}

	buf_len = g_paddr->tz_dbg_info.last_tz_len;
	if (buf_len > LAST_INFO_MAX_LEN) {
		ERROR("panic_log_buf_len = %d. too large", buf_len);
		return -1;
	}

	pbuf = malloc(buf_len);
	if (pbuf == NULL) {
		ERROR("Out of memory\n");
		return -1;
	}

	lseek(fd, LAST_TZ_LOG_BUF_OFFSET, SEEK_SET);
	read(fd, pbuf, buf_len);

	ret = ioctl(f_lk, SET_TZ_LOG_DATA, pbuf);
	if(ret < 0) {
		ERROR("SET_TZ_LOG_DATA fail err=%s.\n", strerror(errno));
		goto exit;
	}
	return 0;
exit:
	if (pbuf)
		free(pbuf);
	return ret;
}

int main_last_rpmlog(int fd)
{
	int f_lk = 0;
	char *pbuf = NULL;
	char *pcfg = NULL;
	int buf_len;
	int cfg_len;
	int ret = 0;

	if (g_pinfo == NULL)
		goto exit;

	f_lk = open(FILE_LAST_RPM_DBG, O_RDONLY);
	if (f_lk < 0) {
		ERROR("%s is not avail.\n",FILE_LAST_RPM_DBG);
		return -1;
	}
	/*get rpm cfg data, and alloc cfg buf*/
	cfg_len = g_paddr->rpm_dbg_info.last_rpm_cfg_len;
	if (cfg_len > LAST_INFO_MAX_LEN) {
		ERROR("last_rpm_cfg_len = %d. too large", cfg_len);
		return -1;
	}

	pcfg = malloc(cfg_len);
	if (pcfg == NULL) {
		ERROR("pcfg alloc failed.\n");
		return -1;
	}

	/*get rpm log data, and alloc log buf*/
	buf_len = g_paddr->rpm_dbg_info.last_rpm_len;
	if (buf_len > LAST_INFO_MAX_LEN) {
		ERROR("last_rpm_len = %d. too large", buf_len);
		return -1;
	}

	pbuf = malloc(buf_len);
	if (pbuf == NULL) {
		ERROR("pbuf alloc failed.\n");
		return -1;
	}

	/*read and then set rpm cfg data to kernel*/
	lseek(fd, LAST_RPM_CFG_BUF_OFFSET, SEEK_SET);
	read(fd, pcfg, cfg_len);

	ret = ioctl(f_lk, SET_RPM_CFG_DATA, pcfg);
	if(ret < 0) {
		ERROR("SET_RPM_CFG_DATA fail err=%s.\n", strerror(errno));
		goto exit;
	}

	/*read and then set rpm log data to kernel*/
	lseek(fd, LAST_RPM_LOG_BUF_OFFSET, SEEK_SET);
	read(fd, pbuf, buf_len);

	ret = ioctl(f_lk, SET_RPM_LOG_DATA, pbuf);
	if(ret < 0) {
		ERROR("SET_RPM_LOG_DATA fail err=%s.\n", strerror(errno));
		goto exit;
	}
	return 0;
exit:
	if (pbuf)
		free(pbuf);
	if (pcfg)
		free(pcfg);
	return ret;
}

static void dt_free(char **buffer)
{
	free(*buffer);
	*buffer = 0;
}

static int dt_read_file(const char *file, char **buffer, int len)
{
	int read;
	FILE *fp;
	if(*buffer == NULL)
		*buffer = (char *)malloc(len);
	if (!(*buffer)) {
		ERROR("%s(): malloc(%d) failed\n", __func__, len);
		return 0;
	}

	fp = fopen(file, "r");
	if (!fp) {
		ERROR("%s(): Failed to open %s: ", __func__, file);
		ERROR("Error: %s\n", strerror(errno));
		snprintf(*buffer,sizeof(*buffer),"%s","unknown");
		return sizeof("unknown");
	}
	read = fread(*buffer, sizeof(char), len, fp);
	fclose(fp);
	return read;
}


int get_panic_reason(char ** data)
{
	int read_cnt;
	read_cnt = dt_read_file("/proc/lst_pnc_rsn", data, 100);
	return read_cnt;
}

#define LAST_KMSG_DIR ("/sdcard/logs/last_kmsg")
#define LAST_PANIC_KLOG ("last_panic_klog")
#define SYS_SSR_TYPE "sys.ssr.type"
#define SYS_SSR_INFO "sys.ssr.info"
#define SYS_SSR_FLAG "sys.ssr.flag"

int main_read_last_info(void)
{
	int try_cnt = 5;
	char cmd[PATH_MAX];
	char tmp_buf[PATH_MAX];
	char reset_type[]="SYS_RESET";
	char *reset_reason = NULL;
	struct timeval tv;
    time_t curtime;
    struct tm *tm_ptr = NULL;
    int ret;

	if (access(LAST_KMSG_DIR,W_OK)!=0){
try_again:
    // create TOP_LOG_DIR if doesn't exist
        ret = create_log_dir(LAST_KMSG_DIR, 0666);
        if(errno != EEXIST && (ret < 0)){
            ERROR("create dir %s failed with error %s, will try %d times later",LAST_KMSG_DIR,strerror(errno),try_cnt);
            if(try_cnt-- > 0) {
                sleep(1);
                goto try_again;
            }
            else
            goto out;
	    }
	}

    gettimeofday(&tv, NULL);
    curtime=tv.tv_sec;
    tm_ptr = localtime(&curtime);
    if (tm_ptr)
        strftime(tmp_buf, 30, "_%Y%m%d_%H%M%S", tm_ptr);
    else
        strlcpy(tmp_buf, "_00000000_000000", 30);

    get_panic_reason(&reset_reason);
    if(reset_reason != NULL){
        if(!strncmp(reset_reason,"SUBSYS_RESET",sizeof("SUBSYS_RESET")) ||
            !strncmp(reset_reason,"PANIC",sizeof("PANIC"))||
            !strncmp(reset_reason,"WDOG_BAK",sizeof("WDOG_BAK"))||
            !strncmp(reset_reason,"WDOG_BITE",sizeof("WDOG_BITE")))
        {
            snprintf(cmd, PATH_MAX, "%s%s%s%s/%s%s%s", "cat ", FILE_LAST_KMSG_PROC, " > ",LAST_KMSG_DIR,LAST_PANIC_KLOG,tmp_buf,".txt");
            NOTICE("reset_reason %s, cmd is %s.\n",reset_reason,cmd);
            system(cmd);
            property_set(SYS_SSR_TYPE, reset_type);
            property_set(SYS_SSR_INFO, reset_reason);
            property_set(SYS_SSR_FLAG, "1");
            dt_free(&reset_reason);
        }
    }
    else
        ERROR("fetch reset_reason failed.\n");

out:
	return 0;
}

int handle_last_info(int fd, int f_panic)
{
	int ret;

	ret = main_panic_reason(fd, f_panic);
	if (ret < 0)
		goto exit;

	if(main_last_kmsg(fd) >= 0)
		main_read_last_info();
	main_last_tzlog(fd);
	main_last_rpmlog(fd);


exit:
	return ret;
}

int is_pmic_reset(void)
{
	int ret = -1;
	char prop_buf[PROPERTY_VALUE_MAX];

	property_get(PROP_PMIC_RESET, prop_buf, "0");
	if (strcmp("1", prop_buf) == 0) {
		ret = 1;
	} else {
		ret = 0;
	}

	return ret;
}

static int is_valid_last(struct last_dbg_info *p)
{
	return p->status == sizeof(struct last_dbg_info);
}

#define LAST_PARTI_PATH ("/dev/block/")

#define LAST_PARTI_SE ("u:object_r:kernellog_device:s0")
#define MAX_SDA_NUM (20)

int get_last_parti_name(char *buf, int buf_len)
{
	int len = 0;
#if 0
	int fd = -1;
	int i = 0, len = 0, ret;
	char *maclabel = NULL;
	int rdump_parti_founded = 0;
	uint64 size, ramsize;

	for (i = 1; i < MAX_SDA_NUM; i++) {
		len = snprintf(buf, buf_len, "%s%d", LAST_PARTI_PATH, i);
		buf[len] = 0;

		lgetfilecon(buf, &maclabel);
		if (!maclabel) {
			continue;
		}

		if(!strcmp(maclabel, LAST_PARTI_SE))
			rdump_parti_founded = 1;

		free(maclabel);
		maclabel = NULL;

		if (rdump_parti_founded)
			break;
	}
#endif
	len = snprintf(buf, buf_len, "%s/%s", LAST_PARTI_PATH, "sda11");
	return true;
}

int main(int argc, char **argv)
{
	struct last_dbg_addr_info last_addr_info;
	struct last_dbg_info last_info;
	u64 ver_crc;
	int fd = 0, f_panic = 0;
	int ret;
	char buf[1024];
	INFO("lastsvc begin\n");

	if (is_pmic_reset()) {
		INFO("pmic_reset is founded.\n");
		goto exit;
	}

	ret = get_last_parti_name(buf, 1024);
	if (ret == 0) {
		ERROR("can't find storage\n");
		goto exit;
	}

	fd = open(buf, O_RDWR);
	f_panic = open(FILE_PANIC_REASON_PROC, O_RDONLY);
	if ((fd < 0) || (f_panic < 0))
		goto exit;

	//read address information and compare version
	lseek(fd, LAST_DBG_HEAD_ADDR_OFFSET , SEEK_SET);
	read(fd, &last_addr_info, sizeof(struct last_dbg_addr_info));

	if (last_addr_info.last_ver_magic != LAST_VERSION_MAGIC) {
		handle_new_kern(fd, f_panic, &last_addr_info);
		ERROR("lastsvc version doesn't match with kernel.\n");
		goto exit;
	}

	ret = ioctl(f_panic, GET_PANIC_REASON_STATUS, &last_addr_info);
	if (ret < 0) {
		ERROR("IOCTL: get panic status fail.\n");
		goto exit;
	} else if (ret == 1) {
		property_set(PROP_PMIC_RESET, "1");
		goto exit;
	} else if (ret == 2) {
		handle_new_kern(fd, f_panic, &last_addr_info);
		goto exit;
	}
	g_paddr = &last_addr_info;

	//read last information
	lseek(fd, LAST_DBG_HEAD_OFFSET , SEEK_SET);
	read(fd, &last_info, sizeof(struct last_dbg_info));
	if (is_valid_last(&last_info)){
		g_pinfo = &last_info;
		//update last info status
		last_info.status = KERN_MAGIC_FOR_XBL;
		lseek(fd, LAST_DBG_HEAD_OFFSET , SEEK_SET);
		write(fd, &last_info, sizeof(struct last_dbg_info));
	} else if (last_info.status == KERN_MAGIC_FOR_XBL) {
		NOTICE("last info alreadly handled.\n");
	} else {
		NOTICE("last info is invalid\n");
	}

	//do main job...
	handle_last_info(fd, f_panic);

exit:
	INFO("lastsvc exit\n");
	if (fd) {
		fsync(fd);
		close(fd);
	}
	if (f_panic)
		close(f_panic);
	return 0;
}
