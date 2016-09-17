package net.michaelsoftware.android.jui;

import android.app.Activity;

/**
 * Created by Michael on 29.08.2016.
 */
public class JuiAction {
    public static void call(JuiParser juiParser, String action) {
        MethodHelper methodHelper = new MethodHelper(juiParser);
        methodHelper.call(action);
    }
}
