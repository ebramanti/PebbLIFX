LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := openssl-android

subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, \
		crypto \
		ssl \
		apps \
	))
	
include $(subdirs)

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
