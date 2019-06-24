LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 android-support-v7-appcompat  adapter-rxjava converter-gson feign-core feign-gson gson logging-interceptor rebound retrofit upload_service commons-net okio_lib okhttp_lib
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4 android-support-v7-recyclerview
LOCAL_STATIC_JAVA_LIBRARIES += annotations-4.5.0 disklrucache-4.5.0 reactive-streams-1.0.1 rxjava-2.0.5 UpgradeLibrary

LOCAL_STATIC_JAVA_AAR_LIBRARIES :=picture_library-debug glide-4.5.0 gifdecoder-4.5.0 rxandroid-2.0.1

LOCAL_SRC_FILES := $(call all-java-files-under, app/src/main/java)

LOCAL_MANIFEST_FILE := app/src/main/AndroidManifest.xml
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/app/src/main/res
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/v7/appcompat/res
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/v7/recyclerview/res

LOCAL_ASSET_DIR := $(LOCAL_PATH)/app/src/main/assets

LOCAL_AAPT_FLAGS := \
        --auto-add-overlay \
        --extra-packages android.support.v7.appcompat \
        --extra-packages android.support.v7.recyclerview\
        --extra-packages com.luck.picture.lib\
        --extra-packages com.bumptech.glide\
        --extra-packages com.bumptech.glide.gifdecoder\
        --extra-packages io.reactivex.android\

#generate version automatically
majorVersion = 1
minorVersion = 0
revisionNumber = $(shell cd $(LOCAL_PATH) && git rev-list --count HEAD)
version_name := $(majorVersion).$(minorVersion).$(revisionNumber).$(shell date +%y%m%d.%H%M).$(shell cd $(LOCAL_PATH) && git describe --always)
LOCAL_AAPT_FLAGS += --version-name $(version_name)

LOCAL_PACKAGE_NAME := Feedback
LOCAL_PRIVILEGED_MODULE := true

#LOCAL_SDK_VERSION := current
LOCAL_RENDERSCRIPT_TARGET_API := 23
LOCAL_JACK_ENABLED := disabled

LOCAL_DEX_PREOPT := false
LOCAL_PROGUARD_ENABLED := disabled
#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := adapter-rxjava:app/libs/adapter-rxjava-2.1.0.jar \
                                        converter-gson:app/libs/converter-gson-2.1.0.jar \
                                        feign-core:app/libs/feign-core-9.5.0.jar \
                                        feign-gson:app/libs/feign-gson-9.5.0.jar \
                                        gson:app/libs/gson-2.7.jar \
                                        logging-interceptor:app/libs/logging-interceptor-3.4.1.jar \
                                        rebound:app/libs/rebound-0.3.8.jar \
                                        retrofit:app/libs/retrofit-2.3.0.jar \
                                        upload_service:app/libs/upload_service.jar \
                                        commons-net:app/libs/commons-net-3.6.jar \
                                        okio_lib:app/libs/okio-1.13.0.jar \
                                        okhttp_lib:app/libs/okhttp-3.8.0.jar \
                                        annotations-4.5.0:app/libs/annotations-4.5.0.jar\
                                        disklrucache-4.5.0:app/libs/disklrucache-4.5.0.jar\
                                        reactive-streams-1.0.1:app/libs/reactive-streams-1.0.1.jar\
                                        rxjava-2.0.5:app/libs/rxjava-2.0.5.jar\
                                        picture_library-debug:app/libs/picture_library-debug.aar\
                                        glide-4.5.0:app/libs/glide-4.5.0.aar\
                                        gifdecoder-4.5.0:app/libs/gifdecoder-4.5.0.aar\
                                        rxandroid-2.0.1:app/libs/rxandroid-2.0.1.aar\

include $(BUILD_MULTI_PREBUILT)
include $(call all-makefiles-under, $(LOCAL_PATH))
