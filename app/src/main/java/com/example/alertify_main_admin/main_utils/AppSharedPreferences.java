package com.example.alertify_main_admin.main_utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSharedPreferences {

    private final SharedPreferences sharedPref;
    private final SharedPreferences.Editor editor;

    public AppSharedPreferences(Context context) {
        String preferencesName = "alertify_high_authority_app_pref";
        sharedPref = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public void put(String key, String value) {
        editor.putString(key, value).apply();
    }

    public void put(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    public void put(String key, int value) {
        editor.putInt(key, value).apply();
    }

    public int getInt(String key) {
        return sharedPref.getInt(key, 0);
    }

    public boolean getBoolean(String key) {
        return sharedPref.getBoolean(key, false);
    }

    public String getString(String key) {
        return sharedPref.getString(key, null);
    }

    public void clear() {
        editor.clear().apply();
    }
}
