package de.michaelsoftware.android.Vision.tools.gui.views;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.ResourceHelper;

/**
 * Created by Michael on 07.04.2016.
 */
public class UploadButton extends Button {
    private String file = null;
    private Context context = null;

    public UploadButton(Context context) {
        super(context);
        this.setText(ResourceHelper.getString(context, R.string.select_file));
        this.context = context;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 10, 0, 10);

        this.setLayoutParams(layoutParams);
    }

    public void setFile(String pFile) {
        this.file = pFile;
        this.setText(ResourceHelper.getString(this.context, R.string.selected_file));
        this.setTextColor(ResourceHelper.getColor(this.context, R.color.darkGreen));
    }

    public String getFile() {
        return file;
    }
}
