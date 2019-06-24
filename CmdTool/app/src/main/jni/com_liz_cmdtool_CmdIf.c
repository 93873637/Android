#include "com_liz_cmdtool_CmdIf.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>
#include <android/log.h>

#include "cmdif.h"

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "CmdTool"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, __VA_ARGS__)

JNIEXPORT jstring JNICALL Java_com_liz_cmdtool_CmdIf_runCmd
(JNIEnv *env, jobject obj, jstring str)
{
    char buf[MAX_OUTPUT_BUF_SIZE] = {0};
    const char *cmd = (*env)->GetStringUTFChars(env, str, 0);

    LOGI("JNI:Java_com_liz_cmdtool_CmdIf_runCmd: cmd=\"%s\"", cmd);

    memset(buf, 0, sizeof(buf));
    exec_command(cmd, buf, sizeof(buf));

    return (*env)->NewStringUTF(env, buf);
}
