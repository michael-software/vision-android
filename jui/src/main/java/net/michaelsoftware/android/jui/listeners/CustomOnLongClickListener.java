package net.michaelsoftware.android.jui.listeners;

import android.app.Activity;
import android.view.View;

import net.michaelsoftware.android.jui.JuiAction;
import net.michaelsoftware.android.jui.JuiParser;

/**
 * Created by Michael on 30.08.2016.
 */
public class CustomOnLongClickListener implements View.OnLongClickListener {
    private JuiParser parser;
    private String action;

    public CustomOnLongClickListener(JuiParser parser, String action) {
        this.parser     = parser;
        this.action     = action;
    }

    @Override
    public boolean onLongClick(View view) {
        JuiAction.call(parser, action);

        return false;
    }
}
