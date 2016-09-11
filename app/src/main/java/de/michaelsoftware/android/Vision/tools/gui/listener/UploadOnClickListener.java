package de.michaelsoftware.android.Vision.tools.gui.listener;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.ResourceHelper;
import de.michaelsoftware.android.Vision.tools.gui.GUIHelper;

/**
 * Created by Michael on 07.04.2016.
 */
public class UploadOnClickListener implements View.OnClickListener {
    GUIHelper guiHelper;

    public UploadOnClickListener(GUIHelper pGuiHelper) {
        this.guiHelper = pGuiHelper;
    }

    @Override
    public void onClick(View v) {
        this.guiHelper.setCurrentView(v);
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        Activity activity = this.guiHelper.getActivity();

        activity.startActivityForResult(Intent.createChooser(intent, ResourceHelper.getString(activity, R.string.select_app)), GUIHelper.REQUEST_FILE);
    }
}
