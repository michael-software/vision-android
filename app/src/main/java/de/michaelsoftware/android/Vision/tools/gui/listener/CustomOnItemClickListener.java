package de.michaelsoftware.android.Vision.tools.gui.listener;

import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;

import de.michaelsoftware.android.Vision.activity.LoginSelectActivity;
import de.michaelsoftware.android.Vision.tools.FormatHelper;

/**
 * Created by Michael on 29.11.2015.
 * Customised onItemClickListener
 * Free for non-commercial use to modify and use
 */
public class CustomOnItemClickListener implements AdapterView.OnItemClickListener {
    private LoginSelectActivity loginSelect;
    private ArrayList<String> actions;

    public CustomOnItemClickListener(ArrayList<String> pArrayActions, LoginSelectActivity pActivity) {
        loginSelect = pActivity;
        actions = pArrayActions;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String action =  actions.get(position);

        if(action != null && FormatHelper.isInt(action)) {

            loginSelect.selectAccount(FormatHelper.stringToInt((String) action, 0));
        }
    }
}
