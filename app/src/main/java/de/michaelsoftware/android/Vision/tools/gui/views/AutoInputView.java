package de.michaelsoftware.android.Vision.tools.gui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.michaelsoftware.android.jui.Tools;

import java.util.ArrayList;

/**
 * Created by Michael on 29.08.2016.
 */
public class AutoInputView extends LinearLayout implements View.OnKeyListener, View.OnClickListener {

    private ArrayList<String> value = new ArrayList<>();
    private ScrollView dataScrollView;
    private LinearLayout dataLinearLayout;
    private EditText editText;
    private int tagBackgroundColor = Color.LTGRAY;

    public AutoInputView(Context context) {
        super(context);

        this.dataScrollView = new ScrollView(context);
            this.dataLinearLayout = new LinearLayout(context);
        this.dataScrollView.addView(dataLinearLayout);
        this.dataScrollView.setPadding(0, 10, 0, 5);

        this.editText = new EditText(context);
            editText.setOnKeyListener(this);
            editText.setSingleLine();
            editText.setMinWidth(Tools.getPxFromDp(context, 100));


        this.addView(dataScrollView);
        this.addView(editText);

        this.setOnClickListener(this);
    }


    public void setValue(ArrayList<String> value) {
        this.value = value;

        this.redrawData();
    }

    private void redrawData() {
        this.dataLinearLayout.removeAllViews();

        for(int i = 0, x = this.value.size(); i < x; i++) {
            this.dataLinearLayout.addView( this.getTagView(this.value.get(i)) );
        }
    }

    private TextView getTagView(String value) {

        TextView tv = new TextView(this.getContext());
        tv.setText(value);


        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 40 );
        shape.setColor(tagBackgroundColor);

        tv.setBackground(shape);
        tv.setPadding(15, 10, 15, 10);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 10, 0);
        tv.setLayoutParams(lp);

        tv.setClickable(true);
        tv.setOnClickListener(this);

        return tv;

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(v == this.editText)
        if(keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_TAB) {

            String text = this.editText.getText().toString();

            if(!text.trim().equals("") && !this.value.contains(text)) {
                this.value.add(text);
                this.redrawData();

                this.editText.setText("");
            }
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        if(v == this) {
            this.editText.requestFocus();
            InputMethodManager lManager = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            lManager.showSoftInput(this.editText, 0);
        } else if(v.getParent() == this.dataLinearLayout && v instanceof TextView) {
            String text = ((TextView) v).getText().toString();
            this.value.remove( this.value.indexOf(text) );

            this.dataLinearLayout.removeView(v);
        }
    }

    public void setTagBackgroundColor(int color) {
        this.tagBackgroundColor = color;
    }

    public ArrayList<String> getValue() {
        return this.value;
    }
}
