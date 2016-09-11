package de.michaelsoftware.android.Vision.service;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import de.michaelsoftware.android.Vision.MyService;
import de.michaelsoftware.android.Vision.tools.Logs;
import de.michaelsoftware.android.Vision.tools.gui.MethodHelper;

/**
 * Created by Michael on 25.03.2016.
 */
public class IncomingHandlerActivity extends Handler {
    Activity activity;

    public IncomingHandlerActivity(Activity pActivity) {
        this.activity = pActivity;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MyService.MSG_SET_INT_VALUE:
                Logs.d(this, "Int Message: " + msg.arg1);
                break;
            case MyService.MSG_SET_STRING_VALUE:
                String str1 = msg.getData().getString("action");
                Logs.d(this, "Str Message: " + str1);
                break;
            case MyService.MSG_SET_ACTION_STRING_VALUE:
                String action = msg.getData().getString("action");
                MethodHelper methodHelper = new MethodHelper(true);
                methodHelper.call(action, this.activity);
                break;
            default:
                super.handleMessage(msg);
        }
    }
}