package com.xj.winemu.sidebar;

import android.content.Context;
import android.content.SharedPreferences;

public class BhFrameGenSettings {
    public static final String PREFS = "bh_framegen";
    public boolean enabled = false;
    public int multiplier = 2;

    public static BhFrameGenSettings load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        BhFrameGenSettings s = new BhFrameGenSettings();
        s.enabled = sp.getBoolean("enabled", false);
        s.multiplier = Math.max(2, Math.min(4, sp.getInt("multiplier", 2)));
        return s;
    }

    public void save(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean("enabled", enabled)
                .putInt("multiplier", Math.max(2, Math.min(4, multiplier)))
                .apply();
    }
}
