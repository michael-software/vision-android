package de.michaelsoftware.android.Vision.tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.michaelsoftware.android.Vision.activity.MainActivity;
import de.michaelsoftware.android.Vision.tools.storage.SharedPreferencesHelper;

/**
 * Created by Michael on 04.03.2016.
 * Class for managing Logs and Developement Toast
 * Log and Dev-Toasts can be enabled and disabled by default and the app settings
 */
public class Logs {
    private static boolean enabled = false;

    @SuppressWarnings("unused") /* Maybe used by other Developers */
    public static int LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static int LENGTH_LONG = Toast.LENGTH_LONG;

    public static void v(Object message) {
        Logs logger = new Logs();
        Logs.v(logger, message);
    }

    public static void d(Object message) {
        Logs logger = new Logs();
        Logs.d(logger, message);
    }

    @SuppressWarnings("unused") /* Maybe used by other Developers */
    public static void w(Object message) {
        Logs logger = new Logs();
        Logs.w(logger, message);
    }

    public static void v(Object caller, Object message) {
        String messageLocal = Logs.getMessage(message);
        String callerLocal = Logs.getCaller(caller);

        if(Logs.isLoged(callerLocal))
        Log.v(callerLocal, messageLocal);
    }

    public static void d(Object caller, Object message) {
        String messageLocal = Logs.getMessage(message);
        String callerLocal = Logs.getCaller(caller);

        if(Logs.isLoged(callerLocal))
        Log.d(callerLocal, messageLocal);
    }

    public static void e(Object caller, Object message) {
        String messageLocal = Logs.getMessage(message);
        String callerLocal = Logs.getCaller(caller);

        if(Logs.isLoged(callerLocal))
        Log.e(callerLocal, messageLocal);
    }

    public static void w(Object caller, Object message) {
        String messageLocal = Logs.getMessage(message);
        String callerLocal = Logs.getCaller(caller);

        if(Logs.isLoged(callerLocal))
        Log.w(callerLocal, messageLocal);
    }

    private static boolean isLoged(String caller) {
        List<String> notLogged = new ArrayList<>();

        return !notLogged.contains(caller) && Logs.enabled;

    }

    private static String getCaller(Object caller) {
        if(caller != null) {
            if (caller instanceof String) {
                return (String) caller;
            } else {
                return caller.getClass().getSimpleName();
            }
        }

        return "";
    }

    private static String getMessage(Object message) {
        if(message != null) {
            if (message instanceof String) {
                return (String) message;
            } else if (FormatHelper.isInt(message)) {
                return message + "";
            } else {
                return message.toString();
            }
        }

        return "";
    }

    public static void toast(Context context, String message, int length) {
        if(Logs.enabled || Logs.isEnabled(context) ) {
            Toast.makeText(context, message, length).show();
        }
    }

    public static boolean isEnabled(Context context) {
        if(context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;

            SharedPreferencesHelper pref = new SharedPreferencesHelper(mainActivity, mainActivity.getLoginHelper().getUsername() + '@' + FormatHelper.getServerName(mainActivity.getLoginHelper().getServer()));
            String developer = pref.read("DEVELOPER", "0");

            if (developer.equals("1")) {
                return true;
            }
        }

        return false;
    }
}
