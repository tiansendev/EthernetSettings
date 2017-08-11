LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := WakeLockService
LOCAL_CERTIFICATE := platform

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res

src_dirs := Java/

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

