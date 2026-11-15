#!/bin/bash
set -x

rm -rf /root/java_build/classes
mkdir -p /root/java_build/classes

javac -source 8 -target 8 -cp /root/android-7.1.1/android.jar -d /root/java_build/classes /sdcard/Wspxce/modmenu/src/com/mod/menu/ModMenu.java /sdcard/Wspxce/modmenu/src/com/fubugames/endlesspuncher/modmenu/ModMenu.java

if [ $? -ne 0 ]; then
    echo "javac compilation failed!"
    exit 1
fi

rm -f /root/java_build/classes.dex

find /root/java_build/classes -type f -name '*.class' -print0 | xargs -0 /root/android-14/d8 --lib /root/android-7.1.1/android.jar --output /root/java_build

if [ $? -ne 0 ]; then
    echo "d8 conversion failed!"
    exit 1
fi

echo "DEX build success:"
ls -lh /root/java_build/classes.dex
