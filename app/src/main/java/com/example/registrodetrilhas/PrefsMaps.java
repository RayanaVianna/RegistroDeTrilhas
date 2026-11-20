package com.example.registrodetrilhas;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.gms.maps.GoogleMap;

public class PrefsMaps {
    private static final String PREF_NAME = "trilhas_prefs";
    private static final String KEY_MAP_TYPE = "map_type";
    private static final String KEY_NAV_MODE = "nav_mode";

    public static final int NAV_NORTH_UP = 0; // Default
    public static final int NAV_COURSE_UP = 1;

    private final SharedPreferences prefs;

    public PrefsMaps(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setTipoMapa(int mapType) {
        prefs.edit().putInt(KEY_MAP_TYPE, mapType).apply();
    }

    public int getTipoMapa() {
        // Vetorial (MAP_TYPE_NORMAL) como padrão
        return prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
    }

    public void setModoNavegacao(int navMode) {
        prefs.edit().putInt(KEY_NAV_MODE, navMode).apply();
    }

    public int getModoNavegacao() {
        // North Up como padrão
        return prefs.getInt(KEY_NAV_MODE, NAV_NORTH_UP);
    }
}