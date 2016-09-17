package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.models.NameValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Michael on 31.08.2016.
 */
public class SpinnerView extends Spinner implements AdapterView.OnItemSelectedListener, View.OnFocusChangeListener {
    private List<NameValue> spinnerArray;
    private String value = null;

    public SpinnerView(Context context) {
        super(context);

        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        this.setOnFocusChangeListener(this);
    }

    public void setValue(HashMap<Object, Object> values) {
        spinnerArray = new ArrayList<>();

        for (int j = 0; j < values.size(); j++) {
            if(values.get(j) instanceof String) {

                if(Tools.isHashmap(values.get(j))) {
                    spinnerArray.add(new NameValue( (String) ((HashMap<Object, Object>) values.get(j)).get(0), (String) ((HashMap<Object, Object>) values.get(j)).get(1)));
                } else if(Tools.isString(values.get(j))) {
                    spinnerArray.add(new NameValue( (String) values.get(j), (String) values.get(j)));
                }
            }
        }

        ArrayAdapter<NameValue> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerArray);

        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.setAdapter(adapter);

        this.setOnItemSelectedListener(this);
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        NameValue nameValue = (NameValue) this.getSelectedItem();
        this.value = nameValue.getValue();
        //this.requestFocus();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        this.value = null;
        //this.requestFocus();
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if(b) {
            this.performClick();
        }
    }
}
