package de.michaelsoftware.android.Vision.tools.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Michael on 24.01.2016.
 * Used for Saving Data at the SharedPreferences
 */
public class SharedPreferencesHelper {
    SharedPreferences settings;

    public SharedPreferencesHelper(Context context, String name) {
        this.settings = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void store(String key, String value) {
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public void storeInt(String key, int value) {
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(key, value);
        editor.apply();
    }

    public void storeBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(key, value);
        editor.apply();
    }

    public String read(String key) {
        return this.settings.getString(key, "");
    }

    public String read(String key, String defaultString) {
        return this.settings.getString(key, defaultString);
    }

    public int readInt(String key) {
        return this.settings.getInt(key, 0);
    }

    public int readInt(String key, int defaultInt) {
        return this.settings.getInt(key, defaultInt);
    }

    public boolean readBoolean(String key) {
        return this.settings.getBoolean(key, false);
    }
}
