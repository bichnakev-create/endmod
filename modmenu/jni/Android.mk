LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := modmenu
LOCAL_SRC_FILES := modmenu.c ../../syscalls.s
LOCAL_LDLIBS    := -llog -ldl
LOCAL_CFLAGS    := -Wall -Wextra -O2
LOCAL_ARM_MODE  := arm64
include $(BUILD_SHARED_LIBRARY)