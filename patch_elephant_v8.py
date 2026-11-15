path = '/root/dex_work/smali_full/com/rollic/elephantsdk/ElephantActivity.smali'
with open(path, 'r') as f:
    lines = f.readlines()

new_lines = []
in_dispatch = False
in_oncreate = False

for line in lines:
    if '.method protected onCreate(Landroid/os/Bundle;)V' in line:
        in_oncreate = True
        new_lines.append(line)
        continue
    if in_oncreate and 'return-void' in line:
        in_oncreate = False
        new_lines.append('''    invoke-super {p0, p1}, Lcom/unity3d/player/UnityPlayerActivity;->onCreate(Landroid/os/Bundle;)V
    :try_start_mod
    const-string v0, "mod"
    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
    :try_end_mod
    .catchall {:try_start_mod .. :try_end_mod} :catch_all_mod
    :catch_all_mod
    return-void
''')
        continue
    if in_oncreate:
        continue

    if '.method public dispatchTouchEvent(Landroid/view/MotionEvent;)Z' in line:
        in_dispatch = True
        new_lines.append(line)
        continue
    if in_dispatch and '.end method' in line:
        in_dispatch = False
        new_lines.append('''    .registers 3
    invoke-static {p0, p1}, Lcom/mod/menu/ModMenu;->onTouch(Landroid/app/Activity;Landroid/view/MotionEvent;)V
    invoke-super {p0, p1}, Lcom/unity3d/player/UnityPlayerActivity;->dispatchTouchEvent(Landroid/view/MotionEvent;)Z
    move-result v0
    return v0
.end method
''')
        continue
    if in_dispatch:
        continue

    new_lines.append(line)

with open(path, 'w') as f:
    f.writelines(new_lines)
print('Updated ElephantActivity.smali successfully!')