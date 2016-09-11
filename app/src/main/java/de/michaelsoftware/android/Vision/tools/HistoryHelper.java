package de.michaelsoftware.android.Vision.tools;

import java.util.ArrayList;
import java.util.List;

import de.michaelsoftware.android.Vision.activity.AbstractMainActivity.BaseActivity;

/**
 * Created by Michael on 22.12.2015.
 * Manages the back event from the mainActivity Class
 * pluginOpen fires the addHistory methode
 */
public class HistoryHelper {
    List<HistoryEntry> historyList;
    BaseActivity activity;

    public HistoryHelper(BaseActivity mainActivity) {
        activity = mainActivity;
        historyList = new ArrayList<>();
    }

    public void addHistory(String pName, String pView, String pCommand) {
        historyList.add(new HistoryEntry(pName, pView, pCommand));
    }

    public void openLastEntry() {
        if(historyList != null) {
            int size = historyList.size() - 1;

            if(size > 0) {
                HistoryEntry last = historyList.get(size - 1);

                activity.openPluginNoHistory(last.name, last.view, last.command);

                historyList.remove(size);
            } else if(size == 0) {
                historyList = new ArrayList<>();
                activity.openPluginNoHistory("android", "home", "");
            } else {
                activity.openPluginNoHistory("android", "home", "");
            }
        } else {
            activity.openPluginNoHistory("android", "home", "");
        }
    }

    public void removeLastEntry() {
        int size = historyList.size() - 2;

        if(historyList != null && size > 0) {
             historyList.remove( size );
        }
    }

    private class HistoryEntry {
        public String name;
        public String view;
        public String command;

        public HistoryEntry(String pName, String pView, String pCommand) {
            this.name = pName;
            this.view = pView;
            this.command = pCommand;
        }
    }
}
