package net.michaelsoftware.android.jui.views;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import net.michaelsoftware.android.jui.Tools;

import java.util.Date;
import java.util.Locale;

/**
 * Created by Michael on 29.08.2016.
 */
public class InputDateView extends Button implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private long value;

    public InputDateView(Context context) {
        super(context);

        long d = Tools.getCurrentTimestamp();

        this.setValue(d);
        this.setText("Datum wählen");

        this.setOnClickListener(this);
    }

    public long getValue() {
        return value;
    }

    public void setValue(long timestamp) {
        value = timestamp;
    }

    public void setValue(int year, int month, int date) {
        Tools.getTimestamp(year, month, date);
    }

    @Override
    public void onClick(View v) {


        Date d = new Date(value);

        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);

        DatePickerDialog dialog = new DatePickerDialog(getContext(), this, Tools.getYear(value), Tools.getMonth(value), Tools.getDate(value));
        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
        setText(date + "." + (month+1) + "." + year);
        setValue(year, month, date);
    }
}
