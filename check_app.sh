DIR=$(dumpsys package com.fubugames.endlesspuncher | /system/bin/grep codePath | awk -F'=' '{print $2}')
echo "DIR is: [$DIR]"
ls -la "$DIR"
ls -la "$DIR/lib"
ls -la "$DIR/lib/arm64"
