package com.mod.menu;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ModMenu {
    public static final String TAG = "CyberMods";
    public static boolean isShowing = false;

    static {
        try {
            System.loadLibrary("modmenu");
            Log.i(TAG, "libmodmenu.so loaded successfully");
        } catch (Throwable t) {
            Log.e(TAG, "Failed loading libmodmenu.so: " + t, t);
        }
    }

    // Native JNI hooks
    public static native double getAmount(int type);
    public static native boolean setAmount(int type, long targetAmount);
    public static native boolean applyCurrencies(int coins, int energy);
    public static native boolean applyGems(int gems);
    public static native float getCurrencyAmount(int type);
    public static native boolean isNoAdsActive();
    public static native boolean setNoAdsActive(boolean enable);
    public static native boolean isDoubleRewardActive();
    public static native boolean setDoubleRewardActive(boolean enable);
    public static native int getChapter();
    public static native int getWave();
    public static native boolean syncStage(int targetChapter, int targetWave);
    public static native int getChestKey(int chestType);
    public static native boolean setChestKey(int chestType, int amount);
    public static native boolean optimizePerformance(int targetFps);
    public static native int getTargetFrameRate();

    // Modern Anime Aesthetic Palette (Sakura & Lavender Dream for Master Keviy)
    public static final int C_BG_START     = Color.parseColor("#1B162E");
    public static final int C_BG_END       = Color.parseColor("#120F20");
    public static final int C_BORDER_MAIN  = Color.parseColor("#FF7597");
    public static final int C_BORDER_ACC   = Color.parseColor("#C084FC");
    public static final int C_PANEL_BG     = Color.parseColor("#80221B3A");
    public static final int C_INPUT_BG     = Color.parseColor("#99151226");
    public static final int C_INPUT_BORDER = Color.parseColor("#66FF7597");
    public static final int C_TEXT_TITLE   = Color.parseColor("#FFFFFF");
    public static final int C_TEXT_SUB     = Color.parseColor("#E9D5FF");
    public static final int C_BTN_START    = Color.parseColor("#FF6584");
    public static final int C_BTN_END      = Color.parseColor("#D946EF");
    public static final int C_BTN_BORDER   = Color.parseColor("#FFAEC0");
    public static final int C_CLOSE_BG     = Color.parseColor("#F43F5E");
    public static final int C_CLOSE_BORDER = Color.parseColor("#FDA4AF");
    public static final int C_TAB_ACTIVE   = Color.parseColor("#FF6584");
    public static final int C_TAB_INACTIVE = Color.parseColor("#4C2A2244");

    public static void dispatchToUnity(final Activity activity, final Runnable runnable) {
        if (activity == null || runnable == null) return;
        try {
            Class<?> actClz = activity.getClass();
            Field mPlayerField = null;
            while (actClz != null && mPlayerField == null) {
                try {
                    mPlayerField = actClz.getDeclaredField("mUnityPlayer");
                } catch (NoSuchFieldException ignored) {
                    actClz = actClz.getSuperclass();
                }
            }
            Object playerObj = null;
            if (mPlayerField != null) {
                mPlayerField.setAccessible(true);
                playerObj = mPlayerField.get(activity);
            }
            if (playerObj == null) {
                try {
                    Method getConn = activity.getClass().getMethod("getUnityPlayerConnection");
                    playerObj = getConn.invoke(activity);
                } catch (Throwable ignored) {}
            }
            if (playerObj != null) {
                Class<?> pClz = playerObj.getClass();
                Method invokeMethod = null;
                while (pClz != null && invokeMethod == null) {
                    try {
                        invokeMethod = pClz.getDeclaredMethod("invokeOnMainThread", Runnable.class);
                    } catch (NoSuchMethodException ignored) {
                        pClz = pClz.getSuperclass();
                    }
                }
                if (invokeMethod != null) {
                    invokeMethod.setAccessible(true);
                    invokeMethod.invoke(playerObj, runnable);
                    return;
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "invokeOnMainThread error: " + t, t);
        }
        new Thread(runnable).start();
    }

    public static void toggle(final Activity activity) {
        if (activity == null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isShowing) {
                    isShowing = true;
                    showCustomMenu(activity);
                }
            }
        });
    }

    public static void onTouch(final Activity activity, MotionEvent event) {
        if (activity == null || event == null) return;
        if (event.getPointerCount() >= 3) {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                toggle(activity);
            }
        }
    }

    public static int dp(Context context, int dpVal) {
        return (int) (dpVal * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static long parseAmountLong(String raw) {
        if (raw == null) return 0L;
        String s = raw.trim().replace(",", "").replace(" ", "").toUpperCase();
        if (s.isEmpty()) return 0L;
        double mult = 1.0;
        char last = s.charAt(s.length() - 1);
        if (last == 'K') { mult = 1e3; s = s.substring(0, s.length() - 1); }
        else if (last == 'M') { mult = 1e6; s = s.substring(0, s.length() - 1); }
        else if (last == 'B') { mult = 1e9; s = s.substring(0, s.length() - 1); }
        try {
            double v = Double.parseDouble(s) * mult;
            if (v < 0) return 0L;
            if (v > 9223372036854775807.0) return Long.MAX_VALUE;
            return (long) v;
        } catch (Exception e) {
            return 0L;
        }
    }

    public static String formatShort(double val) {
        if (val < 0) return "N/A";
        long lVal = (long) val;
        if (lVal >= 1000000000L) {
            return String.format("%.2fB", val / 1e9);
        } else if (lVal >= 1000000L) {
            return String.format("%.2fM", val / 1e6);
        } else if (lVal >= 10000L) {
            return String.format("%.1fK", val / 1e3);
        }
        return String.valueOf(lVal);
    }

    public static EditText makeInput(Activity a, String hint, String defaultText) {
        EditText et = new EditText(a);
        et.setHint(hint);
        et.setHintTextColor(Color.parseColor("#8E84A3"));
        et.setText(defaultText);
        et.setTextColor(Color.WHITE);
        et.setTextSize(14);
        et.setTypeface(Typeface.DEFAULT_BOLD);
        et.setSingleLine(true);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C_INPUT_BG);
        bg.setCornerRadius(dp(a, 16));
        bg.setStroke(dp(a, 1), C_INPUT_BORDER);
        et.setBackground(bg);
        et.setPadding(dp(a, 12), dp(a, 4), dp(a, 12), dp(a, 4));
        return et;
    }

    public static Button makeSetButton(Activity a) {
        Button b = new Button(a);
        b.setText("APPLY");
        b.setTextColor(Color.WHITE);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setAllCaps(true);
        b.setPadding(0, 0, 0, 0);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{C_BTN_START, C_BTN_END});
        bg.setCornerRadius(dp(a, 18));
        bg.setStroke(dp(a, 1), C_BTN_BORDER);
        b.setBackground(bg);
        return b;
    }

    public static class IconView extends View {
        private final int type;
        private final boolean isChestKey;
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        public IconView(Context c, int type) {
            this(c, type, false);
        }

        public IconView(Context c, int type, boolean isChestKey) {
            super(c);
            this.type = type;
            this.isChestKey = isChestKey;
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeWidth(dp(c, 2));

            glow.setStyle(Paint.Style.STROKE);
            glow.setStrokeWidth(dp(c, 1));
        }

        private void poly(float w, float h, float... pts) {
            path.reset();
            for (int i = 0; i < pts.length; i += 2) {
                float x = pts[i] * w, y = pts[i + 1] * h;
                if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
            }
            path.close();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            fill.setStyle(Paint.Style.FILL);

            if (isChestKey) {
                // Stylish Cyberpunk Key Icon
                int keyCol;
                switch (type) {
                    case 0: keyCol = Color.parseColor("#FBBF24"); break; // Wood / Common (Gold)
                    case 1: keyCol = Color.parseColor("#F472B6"); break; // Gem
                    case 2: keyCol = Color.parseColor("#C084FC"); break; // Premium
                    case 3: keyCol = Color.parseColor("#F87171"); break; // Epic / Skull
                    case 4: keyCol = Color.parseColor("#FB923C"); break; // Giant
                    case 5: keyCol = Color.parseColor("#22D3EE"); break; // Collectible
                    case 6: keyCol = Color.parseColor("#FCD34D"); break; // Hero Key
                    case 7: keyCol = Color.parseColor("#34D399"); break; // Equip Gem
                    default: keyCol = Color.parseColor("#E879F9"); break;
                }
                stroke.setColor(keyCol);
                stroke.setStrokeWidth(dp(getContext(), 2));
                // Key Bow (Ring)
                canvas.drawCircle(w * 0.32f, h * 0.35f, w * 0.20f, stroke);
                // Key Shaft
                canvas.drawLine(w * 0.46f, h * 0.48f, w * 0.82f, h * 0.82f, stroke);
                // Key Teeth
                canvas.drawLine(w * 0.68f, h * 0.68f, w * 0.80f, h * 0.58f, stroke);
                canvas.drawLine(w * 0.78f, h * 0.78f, w * 0.90f, h * 0.68f, stroke);
                return;
            }

            if (type == -1) {
                poly(w, h, 0.58f, 0.05f, 0.22f, 0.52f, 0.48f, 0.52f, 0.38f, 0.95f, 0.82f, 0.42f, 0.54f, 0.42f);
                fill.setColor(Color.parseColor("#38BDF8"));
                canvas.drawPath(path, fill);
                stroke.setColor(Color.parseColor("#BAE6FD"));
                canvas.drawPath(path, stroke);
            } else if (type == 0) {
                float r = Math.min(w, h) * 0.42f;
                fill.setColor(Color.parseColor("#FBBF24"));
                canvas.drawCircle(w / 2, h / 2, r, fill);
                stroke.setColor(Color.parseColor("#FDE68A"));
                canvas.drawCircle(w / 2, h / 2, r, stroke);
                glow.setColor(Color.parseColor("#D97706"));
                canvas.drawCircle(w / 2, h / 2, r * 0.6f, glow);
            } else if (type == 7) {
                float rOut = Math.min(w, h) * 0.4f;
                stroke.setColor(Color.parseColor("#34D399"));
                stroke.setStrokeWidth(dp(getContext(), 4));
                canvas.drawCircle(w / 2, h / 2, rOut, stroke);
                glow.setColor(Color.parseColor("#A7F3D0"));
                glow.setStrokeWidth(dp(getContext(), 1));
                canvas.drawCircle(w / 2, h / 2, rOut + dp(getContext(), 2), glow);
                stroke.setStrokeWidth(dp(getContext(), 2));
            } else {
                poly(w, h, 0.25f, 0.18f, 0.75f, 0.18f, 0.95f, 0.42f, 0.5f, 0.92f, 0.05f, 0.42f);
                int gemColor;
                switch (type) {
                    case 1: gemColor = Color.parseColor("#F472B6"); break;
                    case 2: gemColor = Color.parseColor("#38BDF8"); break;
                    case 3: gemColor = Color.parseColor("#A78BFA"); break;
                    case 4: gemColor = Color.parseColor("#FB7185"); break;
                    case 5: gemColor = Color.parseColor("#E879F9"); break;
                    case 6: gemColor = Color.parseColor("#F59E0B"); break;
                    case 8: gemColor = Color.parseColor("#818CF8"); break;
                    case 9: gemColor = Color.parseColor("#F43F5E"); break;
                    default: gemColor = Color.parseColor("#2DD4BF"); break;
                }
                fill.setColor(gemColor);
                canvas.drawPath(path, fill);
                stroke.setColor(Color.parseColor("#FFFFFF"));
                canvas.drawPath(path, stroke);
                glow.setColor(Color.parseColor("#80FFFFFF"));
                canvas.drawLine(w * 0.25f, h * 0.18f, w * 0.5f, h * 0.42f, glow);
                canvas.drawLine(w * 0.75f, h * 0.18f, w * 0.5f, h * 0.42f, glow);
                canvas.drawLine(w * 0.5f, h * 0.42f, w * 0.5f, h * 0.92f, glow);
            }
        }
    }

    public static class CurrencyItem {
        public final int type;
        public final String name;
        public final String colorHex;
        public final int category;
        public final boolean isChestKey;
        public TextView liveView;
        public EditText input;
        public View rowView;

        public CurrencyItem(int type, String name, String colorHex, int category) {
            this(type, name, colorHex, category, false);
        }

        public CurrencyItem(int type, String name, String colorHex, int category, boolean isChestKey) {
            this.type = type;
            this.name = name;
            this.colorHex = colorHex;
            this.category = category;
            this.isChestKey = isChestKey;
        }
    }

    public static class UpdateLabelRunner implements Runnable {
        private final TextView tv;
        private final String text;

        public UpdateLabelRunner(TextView tv, String text) {
            this.tv = tv;
            this.text = text;
        }

        @Override
        public void run() {
            if (tv != null) {
                tv.setText(text);
            }
        }
    }

    public static class UiToastRunner implements Runnable {
        private final Activity act;
        private final String msg;

        public UiToastRunner(Activity act, String msg) {
            this.act = act;
            this.msg = msg;
        }

        @Override
        public void run() {
            if (act != null) {
                Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class SetCurrencyTask implements Runnable {
        private final Activity act;
        private final CurrencyItem item;
        private final long targetVal;

        public SetCurrencyTask(Activity act, CurrencyItem item, long targetVal) {
            this.act = act;
            this.item = item;
            this.targetVal = targetVal;
        }

        @Override
        public void run() {
            boolean ok;
            double newVal;
            if (item.isChestKey) {
                ok = setChestKey(item.type, (int)targetVal);
                newVal = (double)getChestKey(item.type);
            } else {
                ok = setAmount(item.type, targetVal);
                newVal = getAmount(item.type);
            }
            if (ok) {
                act.runOnUiThread(new UpdateLabelRunner(item.liveView, formatShort(newVal)));
                act.runOnUiThread(new UiToastRunner(act, "✦ Set " + item.name + " to " + formatShort(targetVal) + " (o´ω`o)ﾉ"));
            } else {
                act.runOnUiThread(new UiToastRunner(act, "Failed to update " + item.name));
            }
        }
    }

    public static class RefreshAllTask implements Runnable {
        private final Activity act;
        private final List<CurrencyItem> items;

        public RefreshAllTask(Activity act, List<CurrencyItem> items) {
            this.act = act;
            this.items = items;
        }

        @Override
        public void run() {
            for (CurrencyItem item : items) {
                double val;
                if (item.isChestKey) {
                    val = (double)getChestKey(item.type);
                } else {
                    val = getAmount(item.type);
                }
                act.runOnUiThread(new UpdateLabelRunner(item.liveView, formatShort(val)));
            }
        }
    }

    public static class SetButtonListener implements View.OnClickListener {
        private final Activity act;
        private final CurrencyItem item;

        public SetButtonListener(Activity act, CurrencyItem item) {
            this.act = act;
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            if (item.input == null) return;
            String rawText = item.input.getText().toString();
            if (rawText.trim().isEmpty()) {
                Toast.makeText(act, "Please enter an amount first!", Toast.LENGTH_SHORT).show();
                return;
            }
            long targetVal = parseAmountLong(rawText);
            dispatchToUnity(act, new SetCurrencyTask(act, item, targetVal));
        }
    }

    public static class UpdateNoAdsUiRunner implements Runnable {
        private final Activity act;
        private final Button btn;

        public UpdateNoAdsUiRunner(Activity act, Button btn) {
            this.act = act;
            this.btn = btn;
        }

        @Override
        public void run() {
            boolean active = isNoAdsActive();
            btn.setText(active ? "✦ NO-ADS: ON" : "✧ NO-ADS: OFF");
            GradientDrawable b = new GradientDrawable();
            b.setCornerRadius(dp(act, 12));
            if (active) {
                b.setColor(Color.parseColor("#FF6584"));
                b.setStroke(dp(act, 1), Color.parseColor("#FFAEC0"));
                btn.setTextColor(Color.WHITE);
            } else {
                b.setColor(Color.parseColor("#40221B3A"));
                b.setStroke(dp(act, 1), Color.parseColor("#44FF7597"));
                btn.setTextColor(Color.parseColor("#D8B4FE"));
            }
            btn.setBackground(b);
            Toast.makeText(act, active ? "✦ No-Ads Active! (o´ω`o)ﾉ" : "No-Ads Disabled", Toast.LENGTH_SHORT).show();
        }
    }

    public static class UpdateDoubleRewardUiRunner implements Runnable {
        private final Activity act;
        private final Button btn;

        public UpdateDoubleRewardUiRunner(Activity act, Button btn) {
            this.act = act;
            this.btn = btn;
        }

        @Override
        public void run() {
            boolean active = isDoubleRewardActive();
            btn.setText(active ? "✦ 2X CLAIM: ON" : "✧ 2X CLAIM: OFF");
            GradientDrawable b = new GradientDrawable();
            b.setCornerRadius(dp(act, 12));
            if (active) {
                b.setColor(Color.parseColor("#34D399"));
                b.setStroke(dp(act, 1), Color.parseColor("#A7F3D0"));
                btn.setTextColor(Color.WHITE);
            } else {
                b.setColor(Color.parseColor("#40221B3A"));
                b.setStroke(dp(act, 1), Color.parseColor("#4434D399"));
                btn.setTextColor(Color.parseColor("#A7F3D0"));
            }
            btn.setBackground(b);
            Toast.makeText(act, active ? "✦ Instant 2X Rewards Active! (o´ω`o)ﾉ" : "Instant 2X Disabled", Toast.LENGTH_SHORT).show();
        }
    }

    public static class NoAdsToggleTask implements Runnable {
        private final Activity act;
        private final Button btn;

        public NoAdsToggleTask(Activity act, Button btn) {
            this.act = act;
            this.btn = btn;
        }

        @Override
        public void run() {
            boolean nowActive = !isNoAdsActive();
            setNoAdsActive(nowActive);
            act.runOnUiThread(new UpdateNoAdsUiRunner(act, btn));
        }
    }

    public static class UpdateStageUiRunner implements Runnable {
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
    }
    public static class UpdateBoostUiRunner implements Runnable {
        private final Activity act;
        private final boolean ok;
        private final int targetFps;
        private final int activeFps;
        private final Button btn;

        public UpdateBoostUiRunner(Activity act, boolean ok, int targetFps, int activeFps, Button btn) {
            this.act = act;
            this.ok = ok;
            this.targetFps = targetFps;
            this.activeFps = activeFps;
            this.btn = btn;
        }

        @Override
        public void run() {
            if (btn != null) {
                btn.setText(activeFps > 60 ? "⚡ FPS: " + activeFps : "⚡ BOOST 120");
            }
            if (act != null) {
                Toast.makeText(act, ok ? "✦ Engine Boost Active: " + targetFps + " FPS + Shaders Warmed Up + GC Cleared! (o´ω`o)ﾉ" : "Boost Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class OptimizePerformanceTask implements Runnable {
        private final Activity act;
        private final int targetFps;
        private final Button btn;

        public OptimizePerformanceTask(Activity act, int targetFps, Button btn) {
            this.act = act;
            this.targetFps = targetFps;
            this.btn = btn;
        }

        @Override
        public void run() {
            boolean ok = optimizePerformance(targetFps);
            int activeFps = getTargetFrameRate();
            if (act != null) {
                act.runOnUiThread(new UpdateBoostUiRunner(act, ok, targetFps, activeFps, btn));
            }
        }
    }

    public static class DoubleRewardToggleTask implements Runnable {
        private final Activity act;
        private final Button btn;

        public DoubleRewardToggleTask(Activity act, Button btn) {
            this.act = act;
            this.btn = btn;
        }

        @Override
        public void run() {
            boolean nowActive = !isDoubleRewardActive();
            setDoubleRewardActive(nowActive);
            act.runOnUiThread(new UpdateDoubleRewardUiRunner(act, btn));
        }
    }

    public static class CloseClickListener implements View.OnClickListener {
        private final Dialog dialog;

        public CloseClickListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    public static class DialogDismissListener implements DialogInterface.OnDismissListener {
        @Override
        public void onDismiss(DialogInterface d) {
            isShowing = false;
        }
    }

    public static void showCustomMenu(final Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        final List<CurrencyItem> items = new ArrayList<CurrencyItem>();
        // Main Currencies
        items.add(new CurrencyItem(-1, "Energy", "#38BDF8", 1));
        items.add(new CurrencyItem(0, "Coins", "#FBBF24", 1));
        items.add(new CurrencyItem(7, "Rings", "#34D399", 1));
        // Gems
        items.add(new CurrencyItem(1, "Gems", "#F472B6", 2));
        items.add(new CurrencyItem(2, "Diamonds", "#38BDF8", 2));
        items.add(new CurrencyItem(4, "Red Gems", "#FB7185", 2));
        items.add(new CurrencyItem(5, "Pet Gems", "#E879F9", 2));
        items.add(new CurrencyItem(6, "Shop Gems", "#F59E0B", 2));
        // Special Tokens
        items.add(new CurrencyItem(3, "Cubes", "#A78BFA", 3));
        items.add(new CurrencyItem(8, "Tokens", "#818CF8", 3));
        items.add(new CurrencyItem(9, "Pass Exp", "#F43F5E", 3));
        items.add(new CurrencyItem(10, "Hero Exp", "#2DD4BF", 3));
        // Chest Keys (Category 4: KEYS)
        items.add(new CurrencyItem(0, "Common Key", "#FBBF24", 4, true));
        items.add(new CurrencyItem(3, "Epic Key", "#F87171", 4, true));
        items.add(new CurrencyItem(6, "Hero Key", "#FCD34D", 4, true));
        items.add(new CurrencyItem(7, "Gem Chest Key", "#34D399", 4, true));
        items.add(new CurrencyItem(5, "Collec Key", "#22D3EE", 4, true));
        items.add(new CurrencyItem(1, "Gem Key", "#F472B6", 4, true));

        // Outer Modern Anime Card
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(activity, 14), dp(activity, 12), dp(activity, 14), dp(activity, 14));
        
        GradientDrawable cardBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[]{C_BG_START, C_BG_END});
        cardBg.setCornerRadius(dp(activity, 22));
        cardBg.setStroke(dp(activity, 2), C_BORDER_MAIN);
        root.setBackground(cardBg);

        // Header: Personalized Branding for Keviy + Anime Close Badge
        LinearLayout header = new LinearLayout(activity);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hLp.setMargins(0, 0, 0, dp(activity, 10));
        header.setLayoutParams(hLp);

        // Left Anime Icon/Keviy Emblem
        TextView badgeK = new TextView(activity);
        badgeK.setText("✦ K");
        badgeK.setTextColor(Color.parseColor("#FF7597"));
        badgeK.setTextSize(16);
        badgeK.setTypeface(Typeface.DEFAULT_BOLD);
        badgeK.setPadding(dp(activity, 6), dp(activity, 2), dp(activity, 6), dp(activity, 2));
        GradientDrawable kBg = new GradientDrawable();
        kBg.setColor(Color.parseColor("#33FF7597"));
        kBg.setCornerRadius(dp(activity, 12));
        badgeK.setBackground(kBg);
        header.addView(badgeK);

        // Center Title Container (Title + Subtitle)
        LinearLayout titleBox = new LinearLayout(activity);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        titleBox.setLayoutParams(titleLp);

        TextView titleText = new TextView(activity);
        titleText.setText("✦ Keviy Mod Matrix ✦");
        titleText.setTextColor(C_TEXT_TITLE);
        titleText.setTextSize(17);
        titleText.setTypeface(Typeface.DEFAULT_BOLD);
        titleText.setGravity(Gravity.CENTER);
        titleBox.addView(titleText);

        TextView subTitle = new TextView(activity);
        subTitle.setText("Sweet Reality Rewriter (o´ω`o)ﾉ");
        subTitle.setTextColor(C_TEXT_SUB);
        subTitle.setTextSize(10);
        subTitle.setGravity(Gravity.CENTER);
        titleBox.addView(subTitle);

        header.addView(titleBox);

        // Circular Anime Close Button
        Button closeBtn = new Button(activity);
        closeBtn.setText("✕");
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setTextSize(13);
        closeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        closeBtn.setPadding(0, 0, 0, 0);
        closeBtn.setMinHeight(0);
        closeBtn.setMinimumHeight(0);
        closeBtn.setMinWidth(0);
        closeBtn.setMinimumWidth(0);
        GradientDrawable closeBg = new GradientDrawable();
        closeBg.setShape(GradientDrawable.OVAL);
        closeBg.setColor(C_CLOSE_BG);
        closeBg.setStroke(dp(activity, 1), C_CLOSE_BORDER);
        closeBtn.setBackground(closeBg);
        header.addView(closeBtn, new LinearLayout.LayoutParams(dp(activity, 28), dp(activity, 28)));
        root.addView(header);

        // Feature Toggle Card: No-Ads + Instant 2x Double Rewards + Engine Boost
        LinearLayout togglesBox = new LinearLayout(activity);
        togglesBox.setOrientation(LinearLayout.HORIZONTAL);
        togglesBox.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams togLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        togLp.setMargins(0, 0, 0, dp(activity, 10));
        togglesBox.setLayoutParams(togLp);
        togglesBox.setPadding(dp(activity, 8), dp(activity, 6), dp(activity, 8), dp(activity, 6));

        GradientDrawable togCardBg = new GradientDrawable();
        togCardBg.setColor(Color.parseColor("#33221B3A"));
        togCardBg.setCornerRadius(dp(activity, 14));
        togCardBg.setStroke(dp(activity, 1), Color.parseColor("#44FF7597"));
        togglesBox.setBackground(togCardBg);

        // No-Ads Button
        final Button noAdsBtn = new Button(activity);
        noAdsBtn.setTextSize(10);
        noAdsBtn.setTypeface(Typeface.DEFAULT_BOLD);
        noAdsBtn.setPadding(0, 0, 0, 0);
        noAdsBtn.setMinHeight(0);
        noAdsBtn.setMinimumHeight(0);
        noAdsBtn.setMinWidth(0);
        noAdsBtn.setMinimumWidth(0);

        boolean initialNoAds = isNoAdsActive();
        noAdsBtn.setText(initialNoAds ? "✦ NO-ADS" : "✧ NO-ADS");
        GradientDrawable b1 = new GradientDrawable();
        b1.setCornerRadius(dp(activity, 12));
        if (initialNoAds) {
            b1.setColor(Color.parseColor("#FF6584"));
            b1.setStroke(dp(activity, 1), Color.parseColor("#FFAEC0"));
            noAdsBtn.setTextColor(Color.WHITE);
        } else {
            b1.setColor(Color.parseColor("#40221B3A"));
            b1.setStroke(dp(activity, 1), Color.parseColor("#44FF7597"));
            noAdsBtn.setTextColor(Color.parseColor("#D8B4FE"));
        }
        noAdsBtn.setBackground(b1);

        noAdsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchToUnity(activity, new NoAdsToggleTask(activity, noAdsBtn));
            }
        });

        LinearLayout.LayoutParams btn1Lp = new LinearLayout.LayoutParams(0, dp(activity, 30), 1.0f);
        btn1Lp.setMargins(0, 0, dp(activity, 3), 0);
        togglesBox.addView(noAdsBtn, btn1Lp);

        // Instant 2x Rewarded Button
        final Button doubleRewBtn = new Button(activity);
        doubleRewBtn.setTextSize(10);
        doubleRewBtn.setTypeface(Typeface.DEFAULT_BOLD);
        doubleRewBtn.setPadding(0, 0, 0, 0);
        doubleRewBtn.setMinHeight(0);
        doubleRewBtn.setMinimumHeight(0);
        doubleRewBtn.setMinWidth(0);
        doubleRewBtn.setMinimumWidth(0);

        boolean initial2x = isDoubleRewardActive();
        doubleRewBtn.setText(initial2x ? "✦ 2X REWARD" : "✧ 2X REWARD");
        GradientDrawable b2 = new GradientDrawable();
        b2.setCornerRadius(dp(activity, 12));
        if (initial2x) {
            b2.setColor(Color.parseColor("#34D399"));
            b2.setStroke(dp(activity, 1), Color.parseColor("#A7F3D0"));
            doubleRewBtn.setTextColor(Color.WHITE);
        } else {
            b2.setColor(Color.parseColor("#40221B3A"));
            b2.setStroke(dp(activity, 1), Color.parseColor("#4434D399"));
            doubleRewBtn.setTextColor(Color.parseColor("#A7F3D0"));
        }
        doubleRewBtn.setBackground(b2);

        doubleRewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchToUnity(activity, new DoubleRewardToggleTask(activity, doubleRewBtn));
            }
        });

        LinearLayout.LayoutParams btn2Lp = new LinearLayout.LayoutParams(0, dp(activity, 30), 1.0f);
        btn2Lp.setMargins(dp(activity, 3), 0, dp(activity, 3), 0);
        togglesBox.addView(doubleRewBtn, btn2Lp);

        // Engine Boost 120 FPS + GC Clean Button
        final Button boostFpsBtn = new Button(activity);
        boostFpsBtn.setTextSize(10);
        boostFpsBtn.setTypeface(Typeface.DEFAULT_BOLD);
        boostFpsBtn.setPadding(0, 0, 0, 0);
        boostFpsBtn.setMinHeight(0);
        boostFpsBtn.setMinimumHeight(0);
        boostFpsBtn.setMinWidth(0);
        boostFpsBtn.setMinimumWidth(0);
        boostFpsBtn.setText("⚡ BOOST 120");
        GradientDrawable b3 = new GradientDrawable();
        b3.setCornerRadius(dp(activity, 12));
        b3.setColor(Color.parseColor("#6366F1"));
        b3.setStroke(dp(activity, 1), Color.parseColor("#A5B4FC"));
        boostFpsBtn.setTextColor(Color.WHITE);
        boostFpsBtn.setBackground(b3);

        boostFpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchToUnity(activity, new OptimizePerformanceTask(activity, 120, boostFpsBtn));
            }
        });

        LinearLayout.LayoutParams btn3Lp = new LinearLayout.LayoutParams(0, dp(activity, 30), 1.0f);
        btn3Lp.setMargins(dp(activity, 3), 0, 0, 0);
        togglesBox.addView(boostFpsBtn, btn3Lp);

        root.addView(togglesBox);

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
        LinearLayout.LayoutParams stInLp = new LinearLayout.LayoutParams(dp(activity, 45), dp(activity, 26));
        stInLp.setMargins(dp(activity, 4), 0, dp(activity, 4), 0);
        stageBox.addView(stageInput, stInLp);

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

        // Category Tab Switcher Bar
        final LinearLayout tabBar = new LinearLayout(activity);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tabLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(activity, 34));
        tabLp.setMargins(0, 0, 0, dp(activity, 10));
        tabBar.setLayoutParams(tabLp);

        final String[] tabNames = new String[]{"ALL", "MAIN", "GEMS", "SPECIAL", "KEYS"};
        final Button[] tabButtons = new Button[tabNames.length];

        // Scrollable Inner Container
        ScrollView scroll = new ScrollView(activity);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(activity, 330));
        scroll.setLayoutParams(scrollLp);

        final LinearLayout listContainer = new LinearLayout(activity);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(dp(activity, 6), dp(activity, 4), dp(activity, 6), dp(activity, 4));
        
        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setColor(C_PANEL_BG);
        panelBg.setCornerRadius(dp(activity, 16));
        panelBg.setStroke(dp(activity, 1), Color.parseColor("#44C084FC"));
        listContainer.setBackground(panelBg);
        scroll.addView(listContainer);

        // Tab selection listener
        View.OnClickListener tabClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedIndex = (Integer) v.getTag();
                for (int i = 0; i < tabButtons.length; i++) {
                    GradientDrawable tb = new GradientDrawable();
                    tb.setCornerRadius(dp(activity, 14));
                    if (i == clickedIndex) {
                        tb.setColor(C_TAB_ACTIVE);
                        tb.setStroke(dp(activity, 1), Color.parseColor("#FFAEC0"));
                        tabButtons[i].setTextColor(Color.WHITE);
                    } else {
                        tb.setColor(C_TAB_INACTIVE);
                        tb.setStroke(dp(activity, 1), Color.parseColor("#33FF7597"));
                        tabButtons[i].setTextColor(Color.parseColor("#D8B4FE"));
                    }
                    tabButtons[i].setBackground(tb);
                }

                // Filter currency rows
                for (CurrencyItem it : items) {
                    if (it.rowView != null) {
                        if (clickedIndex == 0) {
                            it.rowView.setVisibility(View.VISIBLE);
                        } else if (it.category == clickedIndex) {
                            it.rowView.setVisibility(View.VISIBLE);
                        } else {
                            it.rowView.setVisibility(View.GONE);
                        }
                    }
                }
            }
        };

        for (int i = 0; i < tabNames.length; i++) {
            Button tb = new Button(activity);
            tb.setText(tabNames[i]);
            tb.setTag(i);
            tb.setTextSize(11);
            tb.setTypeface(Typeface.DEFAULT_BOLD);
            tb.setPadding(0, 0, 0, 0);
            tb.setMinHeight(0);
            tb.setMinimumHeight(0);
            tb.setMinWidth(0);
            tb.setMinimumWidth(0);
            GradientDrawable tbBg = new GradientDrawable();
            tbBg.setCornerRadius(dp(activity, 14));
            if (i == 0) {
                tbBg.setColor(C_TAB_ACTIVE);
                tbBg.setStroke(dp(activity, 1), Color.parseColor("#FFAEC0"));
                tb.setTextColor(Color.WHITE);
            } else {
                tbBg.setColor(C_TAB_INACTIVE);
                tbBg.setStroke(dp(activity, 1), Color.parseColor("#33FF7597"));
                tb.setTextColor(Color.parseColor("#D8B4FE"));
            }
            tb.setBackground(tbBg);
            tb.setOnClickListener(tabClick);
            tabButtons[i] = tb;

            LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(0, dp(activity, 28), 1.0f);
            bLp.setMargins(dp(activity, 2), 0, dp(activity, 2), 0);
            tabBar.addView(tb, bLp);
        }
        root.addView(tabBar);

        // Populate items in cards
        for (CurrencyItem item : items) {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.setMargins(0, 0, 0, dp(activity, 6));
            row.setLayoutParams(rowLp);
            row.setPadding(dp(activity, 6), dp(activity, 4), dp(activity, 6), dp(activity, 4));

            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setColor(Color.parseColor("#2617132B"));
            rowBg.setCornerRadius(dp(activity, 12));
            rowBg.setStroke(dp(activity, 1), Color.parseColor("#1AFFFFFF"));
            row.setBackground(rowBg);

            item.rowView = row;

            // Icon
            IconView icon = new IconView(activity, item.type, item.isChestKey);
            row.addView(icon, new LinearLayout.LayoutParams(dp(activity, 32), dp(activity, 32)));

            // Name + Live Badge
            LinearLayout nameCol = new LinearLayout(activity);
            nameCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams ncLp = new LinearLayout.LayoutParams(dp(activity, 82),
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ncLp.setMargins(dp(activity, 8), 0, dp(activity, 4), 0);
            nameCol.setLayoutParams(ncLp);

            TextView nameTv = new TextView(activity);
            nameTv.setText(item.name);
            nameTv.setTextColor(Color.parseColor(item.colorHex));
            nameTv.setTextSize(12);
            nameTv.setTypeface(Typeface.DEFAULT_BOLD);
            nameCol.addView(nameTv);

            item.liveView = new TextView(activity);
            item.liveView.setText("...");
            item.liveView.setTextColor(Color.parseColor("#DDD6FE"));
            item.liveView.setTextSize(10);
            nameCol.addView(item.liveView);

            row.addView(nameCol);

            // Input Well (Pill)
            item.input = makeInput(activity, "e.g. 50K", "");
            LinearLayout.LayoutParams inLp = new LinearLayout.LayoutParams(0, dp(activity, 32), 1.0f);
            inLp.setMargins(0, 0, dp(activity, 6), 0);
            row.addView(item.input, inLp);

            // Apply Button
            Button setBtn = makeSetButton(activity);
            setBtn.setOnClickListener(new SetButtonListener(activity, item));

            row.addView(setBtn, new LinearLayout.LayoutParams(dp(activity, 58), dp(activity, 30)));
            listContainer.addView(row);
        }

        root.addView(scroll);

        // Initial live balances refresh task
        dispatchToUnity(activity, new RefreshAllTask(activity, items));

        closeBtn.setOnClickListener(new CloseClickListener(dialog));
        dialog.setOnDismissListener(new DialogDismissListener());

        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int menuWidth = Math.min(dp(activity, 340), (int)(screenWidth * 0.92f));
        dialog.setContentView(root, new LinearLayout.LayoutParams(
                menuWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
        dialog.show();
    }
}