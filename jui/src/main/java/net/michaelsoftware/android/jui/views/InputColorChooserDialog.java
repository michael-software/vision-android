package net.michaelsoftware.android.jui.views;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import net.michaelsoftware.android.jui.R;
import net.michaelsoftware.android.jui.Tools;

import java.util.ArrayList;

/**
 * Created by Michael on 06.08.2016.
 */
public class InputColorChooserDialog implements SeekBar.OnSeekBarChangeListener {
    private SeekBar sb_red, sb_green, sb_blue;
    private Button ok;
    private TextView tv_preview;
    private Context context;
    private View view;
    private AlertDialog alertDialog;

    private ArrayList<OnColorChangeListener> onColorChangeListeners = new ArrayList<>();

    private int red, blue, green;

    public InputColorChooserDialog(Context context) {
        this.context = context;
        this.view = this.create();
    }

    public View create() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.input_color_chooser_dialog, null);
        sb_red   = (SeekBar) view.findViewById(R.id.inputColorChooserDialog_seekbar_red);
        sb_blue  = (SeekBar) view.findViewById(R.id.inputColorChooserDialog_seekbar_blue);
        sb_green = (SeekBar) view.findViewById(R.id.inputColorChooserDialog_seekbar_green);
        ok    = (Button) view.findViewById(R.id.inputColorChooserDialog_button_ok);
        tv_preview = (TextView) view.findViewById(R.id.inputColorChooserDialog_preview);

        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for (OnColorChangeListener listener : onColorChangeListeners) {
                    listener.onColorChange(Tools.getColor(red, blue, green));
                }

                close();
            }

        });

        sb_red.setOnSeekBarChangeListener(this);
        sb_blue.setOnSeekBarChangeListener(this);
        sb_green.setOnSeekBarChangeListener(this);

        return view;

    }

    public void setColor(int color) {
        this.setColor(Color.red(color), Color.blue(color), Color.green(color));
    }

    public void setColor(int red, int blue, int green) {
        this.red = red;
        this.blue = blue;
        this.green = green;

        this.sb_red.setProgress(red);
        this.sb_blue.setProgress(blue);
        this.sb_green.setProgress(green);

        tv_preview.setBackgroundColor(Tools.getColor(red, blue, green));
        tv_preview.setTextColor(Tools.getContrastColor(red, blue, green));

        tv_preview.setText(Tools.getHex(red, blue, green));
    }

    public void close() {
        if(alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog.cancel();
            alertDialog = null;
        }
    }

    public void show() {
        if(alertDialog != null) {
            this.close();
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.Dialog);

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(view);
        alertDialogBuilder.setView(scrollView);
        alertDialogBuilder.setCancelable(true);
        this.alertDialog = alertDialogBuilder.create();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(this.alertDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        try {
            this.alertDialog.show();
            this.alertDialog.getWindow().setAttributes(lp);
        } catch (Exception e) {
            // WindowManager$BadTokenException will be caught and the app would
            // not display the 'Force Close' message
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser) {
            if (seekBar == sb_red) {
                red = progress;
            } else if (seekBar == sb_blue) {
                blue = progress;
            } else if (seekBar == sb_green) {
                green = progress;
            }

            this.setColor(red, blue, green);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void setOnColorChangeListener(OnColorChangeListener listener) {
        onColorChangeListeners.add(listener);
    }

    public interface OnColorChangeListener {
        public void onColorChange(int color);
    }
}