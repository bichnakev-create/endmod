package com.fubugames.endlesspuncher.modmenu;

import android.app.Activity;

class ModMenu {
    public static void toggle(Activity activity) {
        if (activity != null) {
            com.mod.menu.ModMenu.toggle(activity);
        }
    }
}