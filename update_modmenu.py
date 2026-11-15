import sys

with open('/sdcard/Wspxce/modmenu/src/com/mod/menu/ModMenu.java', 'r') as f:
    text = f.read()

# 1. Native methods
decl_anchor = '    public static native boolean setDoubleRewardActive(boolean enable);\n'
new_decls = '''    public static native boolean setDoubleRewardActive(boolean enable);
    public static native int getChapter();
    public static native int getWave();
    public static native boolean syncStage(int targetChapter, int targetWave);
'''

if 'public static native int getChapter();' not in text:
    assert decl_anchor in text, 'decl_anchor not found'
    text = text.replace(decl_anchor, new_decls, 1)

# 2. StageSyncTask
task_anchor = '    public static class DoubleRewardToggleTask implements Runnable {'
new_tasks = '''    public static class StageSyncTask implements Runnable {
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
    }
    public static class DoubleRewardToggleTask implements Runnable {'''

if 'public static class StageSyncTask' not in text:
    assert task_anchor in text, 'task_anchor not found'
    text = text.replace(task_anchor, new_tasks, 1)

# 3. Stage Sync UI block
toggles_anchor = '        root.addView(togglesBox);\n'
new_stage_ui = '''        root.addView(togglesBox);

        // Stage & Wave Synchronizer Box (Unlocks & fixes round progression)
        LinearLayout stageBox = new LinearLayout(activity);
        stageBox.setOrientation(LinearLayout.HORIZONTAL);
        stageBox.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams stageLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stageLp.setMargins(0, 0, 0, dp(activity, 10));
        stageBox.setLayoutParams(stageLp);
        stageBox.setPadding(dp(activity, 8), dp(activity, 6), dp(activity, 8), dp(activity, 6));

        GradientDrawable stageCardBg = new GradientDrawable();
        stageCardBg.setColor(Color.parseColor("#33221B3A"));
        stageCardBg.setCornerRadius(dp(activity, 14));
        stageCardBg.setStroke(dp(activity, 1), Color.parseColor("#4438BDF8"));
        stageBox.setBackground(stageCardBg);

        final TextView stageStatusTxt = new TextView(activity);
        stageStatusTxt.setTextSize(10);
        stageStatusTxt.setTypeface(Typeface.DEFAULT_BOLD);
        stageStatusTxt.setTextColor(Color.parseColor("#38BDF8"));
        int initCh = getChapter();
        int initWv = getWave();
        stageStatusTxt.setText("Stage: " + (initCh >= 0 ? (initCh + 1) : "?") + " (W:" + initWv + ")");
        LinearLayout.LayoutParams stTxtLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        stageBox.addView(stageStatusTxt, stTxtLp);

        // Input target stage
        final EditText stageInput = new EditText(activity);
        stageInput.setTextSize(11);
        stageInput.setTextColor(Color.WHITE);
        stageInput.setHint("Stg");
        stageInput.setHintTextColor(Color.parseColor("#88FFFFFF"));
        stageInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        stageInput.setGravity(Gravity.CENTER);
        stageInput.setPadding(dp(activity, 4), dp(activity, 2), dp(activity, 4), dp(activity, 2));
        stageInput.setText(String.valueOf(initCh >= 0 ? (initCh + 1) : 1));
        GradientDrawable stInBg = new GradientDrawable();
        stInBg.setColor(C_INPUT_BG);
        stInBg.setCornerRadius(dp(activity, 8));
        stInBg.setStroke(dp(activity, 1), Color.parseColor("#4438BDF8"));
        stageInput.setBackground(stInBg);
        LinearLayout.LayoutParams inLp = new LinearLayout.LayoutParams(dp(activity, 45), dp(activity, 26));
        inLp.setMargins(dp(activity, 4), 0, dp(activity, 4), 0);
        stageBox.addView(stageInput, inLp);

        // SYNC Button
        final Button syncBtn = new Button(activity);
        syncBtn.setText("⚡ SYNC");
        syncBtn.setTextSize(10);
        syncBtn.setTypeface(Typeface.DEFAULT_BOLD);
        syncBtn.setTextColor(Color.WHITE);
        syncBtn.setPadding(0, 0, 0, 0);
        syncBtn.setMinHeight(0);
        syncBtn.setMinimumHeight(0);
        syncBtn.setMinWidth(0);
        syncBtn.setMinimumWidth(0);
        GradientDrawable syncBg = new GradientDrawable();
        syncBg.setColor(Color.parseColor("#0284C7"));
        syncBg.setCornerRadius(dp(activity, 10));
        syncBg.setStroke(dp(activity, 1), Color.parseColor("#38BDF8"));
        syncBtn.setBackground(syncBg);
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String str = stageInput.getText().toString().trim();
                    int targetStg = Integer.parseInt(str);
                    if (targetStg < 1) targetStg = 1;
                    int targetCh = targetStg - 1; // 0-indexed internally
                    dispatchToUnity(activity, new StageSyncTask(activity, targetCh, 0, stageStatusTxt));
                } catch (Throwable t) {
                    Toast.makeText(activity, "Invalid Stage Number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        LinearLayout.LayoutParams syncLp = new LinearLayout.LayoutParams(dp(activity, 65), dp(activity, 28));
        stageBox.addView(syncBtn, syncLp);

        root.addView(stageBox);
'''

if 'Stage & Wave Synchronizer Box' not in text:
    assert toggles_anchor in text, 'toggles_anchor not found'
    text = text.replace(toggles_anchor, new_stage_ui, 1)

with open('/sdcard/Wspxce/modmenu/src/com/mod/menu/ModMenu.java', 'w') as f:
    f.write(text)

print('ModMenu.java updated successfully!')
