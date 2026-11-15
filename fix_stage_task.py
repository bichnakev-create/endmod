with open('/sdcard/Wspxce/modmenu/src/com/mod/menu/ModMenu.java', 'r') as f:
    text = f.read()

# Replace StageSyncTask to avoid anonymous inner classes inside static nested class
old_task = '''    public static class StageSyncTask implements Runnable {
        private final Activity act;
        private final int targetChapter;
        private final int targetWave;
        private final TextView statusView;
        public StageSyncTask(Activity act, int targetChapter, int targetWave, TextView statusView) {
            this.act = act;
            this.targetChapter = targetChapter;
            this.targetWave = targetWave;
            this.statusView = statusView;
        }
        @Override
        public void run() {
            boolean success = syncStage(targetChapter, targetWave);
            final int curCh = getChapter();
            final int curWv = getWave();
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (statusView != null) {
                        statusView.setText("Stage: " + (curCh >= 0 ? (curCh + 1) : "?") + " (W:" + curWv + ")");
                    }
                    Toast.makeText(act, "Stage Synced! Restart or Fight", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }'''

new_task = '''    public static class UpdateStageUiRunner implements Runnable {
        private final Activity act;
        private final TextView statusView;
        private final int curCh;
        private final int curWv;
        public UpdateStageUiRunner(Activity act, TextView statusView, int curCh, int curWv) {
            this.act = act;
            this.statusView = statusView;
            this.curCh = curCh;
            this.curWv = curWv;
        }
        @Override
        public void run() {
            if (statusView != null) {
                statusView.setText("Stage: " + (curCh >= 0 ? (curCh + 1) : "?") + " (W:" + curWv + ")");
            }
            if (act != null) {
                Toast.makeText(act, "Stage Synced! Restart or Fight", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public static class StageSyncTask implements Runnable {
        private final Activity act;
        private final int targetChapter;
        private final int targetWave;
        private final TextView statusView;
        public StageSyncTask(Activity act, int targetChapter, int targetWave, TextView statusView) {
            this.act = act;
            this.targetChapter = targetChapter;
            this.targetWave = targetWave;
            this.statusView = statusView;
        }
        @Override
        public void run() {
            syncStage(targetChapter, targetWave);
            int curCh = getChapter();
            int curWv = getWave();
            if (act != null) {
                act.runOnUiThread(new UpdateStageUiRunner(act, statusView, curCh, curWv));
            }
        }
    }'''

assert old_task in text, "old_task not found"
text = text.replace(old_task, new_task, 1)

with open('/sdcard/Wspxce/modmenu/src/com/mod/menu/ModMenu.java', 'w') as f:
    f.write(text)

print("Replaced StageSyncTask successfully!")
