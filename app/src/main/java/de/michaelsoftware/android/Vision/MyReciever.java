package de.michaelsoftware.android.Vision;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import de.michaelsoftware.android.Vision.tools.Logs;

/**
 * Created by Michael on 27.02.2016.
 */
public class MyReciever  extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals(Intent.ACTION_USER_PRESENT)) {
            Logs.v(this, "Intent recieved:" + action);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, MyService.class);
            PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
            am.cancel(pi);

            double minutes = 1;

            if (minutes > 0) {
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                        SystemClock.elapsedRealtime() + (long)minutes*60*1000,
                        (long) minutes*60*1000, pi);
            }
            /* TODO: Customised (on/off/time) Notification */
        }


    }
}