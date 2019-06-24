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
#include <cutils/log.h>

#define LOG_TAG "cmmonitor"

#define FEEDBACK_PACKAGE_REC_NAME  "com.cloudminds.feedback/.app.FeedbackReceiver"
#define FEEDBACK_BROADCAST_NAME "android.intent.action.FEEDBACK"
#define SYS_SSR_TYPE "sys.ssr.type"
#define SYS_SSR_INFO "sys.ssr.info"
#define SYS_SSR_FLAG "sys.ssr.flag"
#define SSR_RAMDUMP_EN_PROP "persist.sys.ssr.enable_ramdumps"
#define DUMP_EMMC_DIR "/data/ramdump"

#define LAST_KMSG_DIR ("/sdcard/logs/last_kmsg")

int main(int argc, char **argv)
{
    int ret=0,len = 0;
    char reset_type[100];
	char reset_info[100];
    char buf[100];
	char buf_cont[100];
	char buf_cmd[256];
	int ssr_dump_flg = 0;

	ALOGD("enter cmmonitor");
	property_get(SYS_SSR_TYPE, reset_type, "unknown"); 
	property_get(SYS_SSR_INFO, reset_info, "unknown");

	snprintf(buf_cont,sizeof(buf_cont),"%s ,%s",reset_type,reset_info);
	len = strlen(buf_cont);
	buf_cont[len - 1] = 0;

	property_get(SSR_RAMDUMP_EN_PROP, buf, "0");
	if (strcmp(buf, "1") == 0)
		ssr_dump_flg = 1;

	if(strcmp(reset_type, "SYS_RESET") == 0){
		snprintf(buf_cmd, sizeof(buf_cmd), "/system/bin/cmd activity broadcast -n \"%s\" -a \"%s\" --es \"msg_type\" \"%s\" --es \"msg_cont\" \"%s\" --es \"file_path\" \"%s\"",
				FEEDBACK_PACKAGE_REC_NAME, FEEDBACK_BROADCAST_NAME,"SYS_RESET",buf_cont,LAST_KMSG_DIR);
	}
	else
	{
		if(ssr_dump_flg == 1)
			snprintf(buf_cmd, sizeof(buf_cmd), "/system/bin/cmd activity broadcast -n \"%s\" -a \"%s\" --es \"msg_type\" \"%s\" --es \"msg_cont\" \"%s\" --es \"file_path\" \"%s\"",
					FEEDBACK_PACKAGE_REC_NAME, FEEDBACK_BROADCAST_NAME,"SSR_RESET",buf_cont,DUMP_EMMC_DIR);
		else
			snprintf(buf_cmd, sizeof(buf_cmd), "/system/bin/cmd activity broadcast -n \"%s\" -a \"%s\" --es \"msg_type\" \"%s\" --es \"msg_cont\" \"%s\"",
					FEEDBACK_PACKAGE_REC_NAME, FEEDBACK_BROADCAST_NAME,"SSR_RESET",buf_cont);
	}
	property_set(SYS_SSR_TYPE,"unknown");
	property_set(SYS_SSR_INFO,"unknown");
	property_set(SYS_SSR_FLAG,"0");
	ALOGD("run command %s",buf_cmd);
	system(buf_cmd);
}
