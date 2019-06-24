LOCAL_PATH:= $(call my-dir)

$(shell mkdir -p $(TARGET_OUT_ETC)/qxdm/)
$(shell cp $(LOCAL_PATH)/qxdm_mask/modem.cfg $(TARGET_OUT_ETC)/qxdm/modem.cfg)
$(shell cp $(LOCAL_PATH)/qxdm_mask/gps.cfg $(TARGET_OUT_ETC)/qxdm/gps.cfg)
$(shell cp $(LOCAL_PATH)/qxdm_mask/sensor.cfg $(TARGET_OUT_ETC)/qxdm/sensor.cfg)
$(shell cp $(LOCAL_PATH)/qxdm_mask/modem+gps.cfg $(TARGET_OUT_ETC)/qxdm/modem+gps.cfg)
$(shell cp $(LOCAL_PATH)/cmlogd.conf $(TARGET_OUT_ETC)/cmlogd.conf)
#modem log as default logmask
$(shell cp $(LOCAL_PATH)/qxdm_mask/1.cfg $(TARGET_OUT_ETC)/qxdm/1.cfg)
$(shell cp $(LOCAL_PATH)/qxdm_mask/2.cfg $(TARGET_OUT_ETC)/qxdm/2.cfg)

include $(CLEAR_VARS)
LOCAL_CFLAGS:= -Wall -O2
LOCAL_SRC_FILES:= cmlogd.c utils.c
LOCAL_MODULE:= cmlogd
LOCAL_SHARED_LIBRARIES := libcutils liblog
ifeq ($(PRODUCT_SW_VER),STABLE_SW)
ifeq ($(TARGET_BUILD_VARIANT),user)
  LOCAL_CFLAGS += -DLE_STABLE_SW
endif
endif
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_CFLAGS:= -Wall -O2
LOCAL_SRC_FILES:= cmassit.c utils.c
LOCAL_MODULE:= cmassit
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_SHARED_LIBRARIES := libcutils liblog
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_CFLAGS:= -Wall -O2
LOCAL_SRC_FILES:= cmmonitor.c utils.c
LOCAL_MODULE:= cmmonitor
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_SHARED_LIBRARIES := libcutils liblog
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_CFLAGS:= -Wall -O2
LOCAL_SRC_FILES:= cmkmsg.c utils.c
LOCAL_MODULE:= qlogd
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_SHARED_LIBRARIES := libcutils liblog
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_CFLAGS:= -Wall -O2
LOCAL_SRC_FILES:= cmqsee.c utils.c
LOCAL_MODULE:= qlogd_qsee
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_SHARED_LIBRARIES := libcutils liblog
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_CFLAGS:= -Wall -O2
LOCAL_SRC_FILES:= cmtz.c utils.c
LOCAL_MODULE:= qlogd_tz
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_SHARED_LIBRARIES := libcutils liblog
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_CFLAGS:= -Wall -O2
LOCAL_SRC_FILES:= cmsysprof.c utils.c
LOCAL_MODULE:= qlogd_prof
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_SHARED_LIBRARIES := libcutils liblog
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_CFLAGS:= -Wall -O2
LOCAL_SRC_FILES:= rdumpsvc.c utils.c
LOCAL_MODULE:= rdumpsvc
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_SHARED_LIBRARIES := libcutils libselinux liblog
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_CFLAGS:= -Wall -O2
LOCAL_SRC_FILES:= rdumpextract.c utils.c
LOCAL_MODULE:= rdumpextra
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_SHARED_LIBRARIES := libcutils libselinux liblog
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_CFLAGS:= -Wall -O2
LOCAL_SRC_FILES:= lastsvc.c utils.c
LOCAL_MODULE:= lastsvc
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_STATIC_LIBRARIES:=libinit liblog
LOCAL_SHARED_LIBRARIES := libcutils libselinux liblog
include $(BUILD_EXECUTABLE)

