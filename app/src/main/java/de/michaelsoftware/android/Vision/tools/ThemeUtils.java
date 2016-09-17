package de.michaelsoftware.android.Vision.tools;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.storage.SharedPreferencesHelper;

/**
 * Created by Michael on 08.04.2016.
 */
public class ThemeUtils {
    private static int cTheme;

    public final static int LIGHT = 0;
    public final static int DARK = 1;

    public static void changeToTheme(Activity activity, int theme) {
        cTheme = theme;
        activity.finish();

        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    public static void changeToTheme(Activity activity, int theme, LoginHelper loginHelper) {
        SharedPreferencesHelper pref = new SharedPreferencesHelper(activity, loginHelper.getIdentifier());
        pref.storeInt("THEME", theme);

        cTheme = theme;
        activity.finish();

        Intent call = new Intent(activity, activity.getClass());
        call.putExtra("account", loginHelper.getAccount());
        activity.startActivity(call);
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        switch (cTheme) {
            default:
            case LIGHT:
                activity.setTheme(R.style.AppThemeLight);
                break;

            case DARK:
                activity.setTheme(R.style.AppThemeDark);
                break;
        }
    }

    public static void onActivityCreateSetTheme(Activity activity, String identifier) {
        Log.d("Set theme", "Theme:" + identifier);

        cTheme = ThemeUtils.getTheme(activity, identifier);

        switch (cTheme) {
            default:
            case LIGHT:
                activity.setTheme(R.style.AppThemeLight);
                break;

            case DARK:
                activity.setTheme(R.style.AppThemeDark);
                break;
        }
    }

    public static int getCurrentTheme() {
        return cTheme;
    }

    public static int getTheme(Activity activity, String identifier) {
        SharedPreferencesHelper pref = new SharedPreferencesHelper(activity, identifier);
        return pref.readInt("THEME");
    }

    public static int getThemeRessource(Resources.Theme theme, int attr){
        TypedValue typedvalueattr = new TypedValue();
        theme.resolveAttribute(attr, typedvalueattr, true);
        return typedvalueattr.resourceId;
    }
}