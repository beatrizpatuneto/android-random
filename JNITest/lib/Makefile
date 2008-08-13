CC=arm-none-linux-gnueabi-gcc
LD=arm-none-linux-gnueabi-ld

CPPFLAGS=-I/usr/lib/jvm/java-1.5.0-sun/include -I/usr/lib/jvm/java-1.5.0-sun/include/linux
CFLAGS=-fpic
LDFLAGS=-T armelf_linux_eabi.xsc -shared

TARGET = libNativeMD5.so
SOURCES = org_devtcg_demo_jnitest_NativeMD5.c md5.c
OBJECTS = $(SOURCES:%.c=%.o)

$(TARGET): $(OBJECTS)
	$(LD) $(LDFLAGS) $(OBJECTS) -o $@

clean:
	rm -f $(TARGET) $(OBJECTS)
