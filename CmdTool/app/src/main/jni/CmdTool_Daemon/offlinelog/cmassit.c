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

#define CMLOGD_LOG_SAVE_PROP ("persist.sys.cmlogd.save")
#define CMLOGD_LOG_PATH ("persist.sys.cmlogd.path")

int main(int argc, char **argv)
{
    int ret=0;
    char buf[PROPERTY_VALUE_MAX];
    char path[PATH_MAX];

    ret = property_get(CMLOGD_LOG_SAVE_PROP, buf, NULL);
    if ((ret <= 0) || (strcmp(buf, "0") == 0))
    {
        property_get(CMLOGD_LOG_PATH, path,NULL);
        ALOGD("remove logs under %s\n", path);
        snprintf(buf,PROPERTY_VALUE_MAX,"%s/%s","rm -rf ",path);
        if (system(buf) != 0) {
            ALOGE("remove logs under %s failed.\n", path);
        }
    }
    else//copy tombstone and anr log
    {
        property_get(CMLOGD_LOG_PATH, path,NULL);
        ALOGD("copy anr and tombstone into %s\n", path);
        snprintf(buf,PROPERTY_VALUE_MAX,"%s %s","cp -r /data/anr ",path);
        if (system(buf) != 0) {
            ALOGE("cp anr logs into %s failed.\n", path);
        }

        snprintf(buf,PROPERTY_VALUE_MAX,"%s","rm /data/anr/* ");
        if (system(buf) != 0) {
            ALOGE("rm /data/anr/ failed.\n");
        }

        snprintf(buf,PROPERTY_VALUE_MAX,"%s %s","cp -r /data/tombstones ",path);
        if (system(buf) != 0) {
            ALOGE("cp tombstone logs into %s failed.\n", path);
        }

        snprintf(buf,PROPERTY_VALUE_MAX,"%s","rm /data/tombstones/*");
        if (system(buf) != 0) {
            ALOGE("rm /data/tombstones/* failed.\n");
        }

        snprintf(buf,PROPERTY_VALUE_MAX,"%s %s%s","mv /sdcard/tsens_logger.csv ",path,"/charge_logger");
        if (system(buf) != 0) {
            ALOGE("mv tsens_logger into %s failed.\n", path);
        }
    }
    ret = property_set(CMLOGD_LOG_SAVE_PROP, "2");
    if (ret <= 0)
        ALOGE("set CMLOGD_LOG_SAVE_PROP to default value failed.\n");

	return ret;
}
