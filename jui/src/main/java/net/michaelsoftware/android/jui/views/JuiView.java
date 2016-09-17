package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.view.View;

import net.michaelsoftware.android.jui.JuiParser;

import java.util.HashMap;

/**
 * Created by Michael on 28.08.2016.
 */
public abstract class JuiView {
    protected Context context;

    public JuiView(Context activity) {
        this.context = activity;
    }

    public JuiView(Context activity, HashMap<Object, Object> hashMap) {
        this.context = activity;
    }

    public abstract View getView(JuiParser parser);
}
