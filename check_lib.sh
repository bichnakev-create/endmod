DIR=$(dumpsys package com.fubugames.endlesspuncher | /system/bin/grep codePath | tr -d '' | awk -F'=' '{print $2}')
ls -la "$DIR"
ls -la "$DIR/lib"
ls -la "$DIR/lib/arm64"
