package com.fubugames.endlesspuncher.modmenu;

import android.app.Activity;

/**
 * Public compatibility bridge retained because ElephantActivity in classes10.dex
 * invokes this exact descriptor. The visual implementation lives in
 * com.mod.menu.ModMenu inside classes12.dex.
 */
public final class ModMenu {
    private ModMenu() { }

    public static void toggle(Activity activity) {
        if (activity != null) {
            com.mod.menu.ModMenu.toggle(activity);
        }
    }
}
