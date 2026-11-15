path = '/root/dex_work/smali_full/com/rollic/elephantsdk/ElephantActivity.smali'
with open(path, 'r') as f:
    content = f.read()

old_code = '''    invoke-super {p0, p1}, Lcom/unity3d/player/UnityPlayerActivity;->onCreate(Landroid/os/Bundle;)V
    const-string v0, "modmenu"
    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
    return-void'''

new_code = '''    invoke-super {p0, p1}, Lcom/unity3d/player/UnityPlayerActivity;->onCreate(Landroid/os/Bundle;)V
    :try_start_0
    const-string v0, "modmenu"
    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_all
    :catch_all
    return-void'''

if old_code in content:
    content = content.replace(old_code, new_code)
    with open(path, 'w') as f:
        f.write(content)
    print('ElephantActivity.smali patched with try-catch!')
else:
    print('Pattern not found, please check ElephantActivity.smali')