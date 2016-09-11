package de.michaelsoftware.android.Vision.tools.network;

import de.michaelsoftware.android.Vision.activity.MainActivity;

/**
 * Custom Runnable to access MainActivity in class
 * Created by Michael on 10.05.2016.
 */
public class CustomRunnable implements Runnable {
    private MainActivity mainActivity;

    public CustomRunnable(MainActivity activity) {
        this.mainActivity = activity;
    }

    @Override
    public void run() {
        mainActivity.showProgressDialog();
    }
}
