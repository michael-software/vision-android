package net.michaelsoftware.android.jui.interfaces;

import android.view.View;

import java.util.HashMap;

/**
 * Created by Michael on 07.09.2016.
 */
public class Listener {
    public interface OnSubmitListener {
        String onSubmit(View view);
    }

    public interface OnParseHeaderListener {
        void onParseHead(HashMap<Object, Object> hashMap);
    }

    public interface OnBeforeParseListener {
        boolean onBeforeParse(HashMap<Object, Object> hashMap);
    }
}
