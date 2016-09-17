package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import net.michaelsoftware.android.jui.Tools;

import java.io.File;

/**
 * Created by Michael on 29.08.2016.
 */
public class FileView extends Button implements View.OnClickListener, FileChooserDialog.FileSelectedListener {
    private File value;

    public FileView(Context context) {
        super(context);

        this.setText("Datei auswählen");

        this.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        new FileChooserDialog(getContext()).setFileListener(this).showDialog();
    }

    @Override
    public void fileSelected(File file) {
        this.setValue(file);
    }

    public void setValue(File value) {
        if(value.exists() && value.canRead()) {
            this.setTextColor(Color.GREEN);
            this.setText("Datei ausgewählt");

            this.value = value;
        }
    }

    public String getUri() {
        if(!Tools.empty(this.value)) {
            return this.value.toURI().toString();
        }

        return null;
    }
}
