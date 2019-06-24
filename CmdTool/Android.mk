LOCAL_PATH:= $(call my-dir)

###########################################################
# Build APK 

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 android-support-v7-appcompat
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4 android-support-v7-recyclerview
    
LOCAL_SRC_FILES := $(call all-java-files-under, app/src/main/java)

LOCAL_MANIFEST_FILE := app/src/main/AndroidManifest.xml
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/app/src/main/res
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/v7/appcompat/res
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/v7/recyclerview/res

LOCAL_AAPT_FLAGS := \
        --auto-add-overlay \
        --extra-packages android.support.v7.appcompat \
        --extra-packages android.support.v7.recyclerview\

#generate version automatically
majorVersion = 1
minorVersion = 0
revisionNumber = $(shell cd $(LOCAL_PATH) && git rev-list --count HEAD)
version_name := $(majorVersion).$(minorVersion).$(revisionNumber).$(shell date +%y%m%d.%H%M).$(shell cd $(LOCAL_PATH) && git describe --always)
LOCAL_AAPT_FLAGS += --version-name $(version_name)

LOCAL_PACKAGE_NAME := CmdTool
LOCAL_PRIVILEGED_MODULE := true

#LOCAL_SDK_VERSION := current
LOCAL_RENDERSCRIPT_TARGET_API := 26
LOCAL_JACK_ENABLED := disabled

LOCAL_DEX_PREOPT := false
LOCAL_PROGUARD_ENABLED := disabled
#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional eng
LOCAL_CERTIFICATE := platform

include $(BUILD_MULTI_PREBUILT)
###########################################################

###########################################################
# Build JNI Library 

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= app/src/main/jni/com_liz_cmdtool_CmdIf.c \
                  app/src/main/jni/cmdif.c

# Header files path
LOCAL_C_INCLUDES := \
    app/src/main/jni/ \
    $(JNI_H_INCLUDE) \
    $(call include-path-for, system-core)/cutils

LOCAL_SHARED_LIBRARIES	+= libdl \
                           libutils \
                           libcutils

LOCAL_LDLIBS :=-llog
LOCAL_MODULE_TAGS := optional eng
LOCAL_PRELINK_MODULE := false
LOCAL_CFLAGS += -DLOG_TAG=\"CmdIf\"

ifeq ($(TARGET_SIMULATOR),true)
LOCAL_CFLAGS += -DSINGLE_PROCESS
endif

LOCAL_MODULE:= libCmdIf

include $(BUILD_SHARED_LIBRARY)
###########################################################

include $(call all-makefiles-under, $(LOCAL_PATH))
