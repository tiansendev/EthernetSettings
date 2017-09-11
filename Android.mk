LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := EthernetSettings
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_TAGS := optional

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res \
    frameworks/support/v7/appcompat/res

LOCAL_AAPT_FLAGS := --auto-add-overlay

LOCAL_SRC_FILES := \
    $(call all-java-files-under, src)

include $(BUILD_PACKAGE)

