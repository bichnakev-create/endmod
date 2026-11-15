package com.fubugames.endlesspuncher.modmenu;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;

import com.rollic.elephantsdk.ElephantActivity;
import com.unity3d.player.UnityPlayer;

/**
 * This class injects the mod menu into ElephantActivity.
 * It should be called from ElephantActivity's onCreate method.
 */
public class ModMenuInjector {
    private static final String TAG = "ModMenuInjector";
    private static ModMenu modMenu;
    private static boolean injected = false;
    
    /**
     * Call this from ElephantActivity.onCreate() after super.onCreate()
     */
    public static void inject(Activity activity) {
        if (injected) {
            Log.d(TAG, "Already injected, skipping");
            return;
        }
        
        try {
            Log.d(TAG, "Injecting ModMenu into " + activity.getClass().getSimpleName());
            
            // Initialize mod menu
            modMenu = ModMenu.getInstance(activity);
            modMenu.initialize();
            
            // Hook touch events by wrapping the activity's dispatchTouchEvent
            // We'll do this by creating a wrapper
            injected = true;
            Log.d(TAG, "ModMenu injection complete");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to inject ModMenu", e);
        }
    }
    
    /**
     * Call this from ElephantActivity.dispatchTouchEvent() to handle 3-finger tap
     */
    public static boolean onTouchEvent(MotionEvent event) {
        if (modMenu != null) {
            return modMenu.onTouchEvent(event);
        }
        return false;
    }
    
    public static boolean isInjected() {
        return injected;
    }
    
    public static ModMenu getModMenu() {
        return modMenu;
    }
}
