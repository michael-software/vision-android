package de.michaelsoftware.android.Vision.tools.gui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.michaelsoftware.android.jui.Tools;

import de.michaelsoftware.android.Vision.R;

/**
 * Created by Michael on 06.08.2016.
 */
public class EditorView extends LinearLayout implements View.OnClickListener {
    private WebView wvContent;
    private HorizontalScrollView scrollViewControl;
    private LinearLayout horizontalLinearLayoutControl;
    private Button btnBold, btnItalic, btnUnderline;
    private ImageButton imgBtnLeft, imgBtnCenter, imgBtnRight, imgBtnFull;
    private ImageButton imgBtnUnorderedList, imgBtnOrderedList;
    private String currentContent = "";

    public EditorView(Context context) {
        super(context);

        this.setOrientation(VERTICAL);

        this.setBackgroundColor(Color.argb(230, 235, 235, 235));

        wvContent = new WebView(context);
        WebSettings webSettings = wvContent.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvContent.addJavascriptInterface(new MyJavaScriptInterface(this), "android");
        wvContent.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (250 * getResources().getDisplayMetrics().density)));
        wvContent.setBackgroundColor(Color.TRANSPARENT);

        wvContent.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int historySize = event.getHistorySize();
                    if(historySize > 0) {
                        int y = (int) event.getHistoricalY(historySize-1);

                        if (view.getScrollY() > 0 || (event.getY() - y) < 0) {
                            requestDisallowInterceptTouchEvent(true);
                        } else {
                            requestDisallowInterceptTouchEvent(false);
                        }
                    }
                }

                return view.onTouchEvent(event);
            }
        });

        this.setUpControls();

        this.addView(wvContent);
    }

    private String getHTMLDocument(String value) {
        return "<html><head>" +
                "<style rel=\"stylesheet\">" +
                "body { padding: 0; margin: 0; background-color: transparent; } #content { width: 100%; padding: 15px; box-sizing: border-box; overflow: scroll;  }" +
                "</style>" +
                "</head><body><div id=\"content\" contenteditable=\"true\">" +
                Tools.removeJavascript(value) + "</div><script>" +
                "var content = document.getElementById('content');" +
                "content.addEventListener(\"keydown\", function() {" +
                    "android.setWebViewContentValue(content.innerHTML);" +
                "});" +
                "function getWebViewContentValue() {" +
                    "android.setWebViewContentValue(content.innerHTML);" +
                "}" +
                "</script></body></html>";
    }

    private void setUpControls() {
        scrollViewControl             = new HorizontalScrollView(getContext());
        horizontalLinearLayoutControl = new LinearLayout(getContext());
        horizontalLinearLayoutControl.setOrientation(HORIZONTAL);
        scrollViewControl.addView(horizontalLinearLayoutControl);
        scrollViewControl.setHorizontalScrollBarEnabled(false);
        scrollViewControl.setBackgroundColor(Color.argb(230, 150, 150, 150));
        this.addView(scrollViewControl);


        btnBold = new Button(getContext());
        btnBold.setText("B");
        btnBold.setTypeface(null, Typeface.BOLD);
        btnBold.setOnClickListener(this);
        btnBold.setBackgroundColor(Color.GRAY);
        btnBold.setMinimumWidth(250);
        this.horizontalLinearLayoutControl.addView(btnBold);

        btnItalic = new Button(getContext());
        btnItalic.setText("I");
        btnItalic.setTypeface(null, Typeface.ITALIC);
        btnItalic.setOnClickListener(this);
        btnItalic.setBackgroundColor(Color.LTGRAY);
        btnItalic.setMinimumWidth(250);
        this.horizontalLinearLayoutControl.addView(btnItalic);

        btnUnderline = new Button(getContext());
        btnUnderline.setText("U");
        btnUnderline.setPaintFlags(btnUnderline.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        btnUnderline.setOnClickListener(this);
        btnUnderline.setBackgroundColor(Color.GRAY);
        btnUnderline.setMinimumWidth(250);
        this.horizontalLinearLayoutControl.addView(btnUnderline);

        imgBtnLeft = new ImageButton(getContext());
        imgBtnLeft.setImageResource(R.drawable.ic_action_justify_left_black);
        imgBtnLeft.setOnClickListener(this);
        imgBtnLeft.setBackgroundColor(Color.LTGRAY);
        imgBtnLeft.setMinimumWidth(250);
        this.horizontalLinearLayoutControl.addView(imgBtnLeft);

        imgBtnCenter = new ImageButton(getContext());
        imgBtnCenter.setImageResource(R.drawable.ic_action_justify_center_black);
        imgBtnCenter.setOnClickListener(this);
        imgBtnCenter.setBackgroundColor(Color.GRAY);
        imgBtnCenter.setMinimumWidth(250);
        this.horizontalLinearLayoutControl.addView(imgBtnCenter);

        imgBtnRight = new ImageButton(getContext());
        imgBtnRight.setImageResource(R.drawable.ic_action_justify_right_black);
        imgBtnRight.setOnClickListener(this);
        imgBtnRight.setBackgroundColor(Color.LTGRAY);
        imgBtnRight.setMinimumWidth(250);
        this.horizontalLinearLayoutControl.addView(imgBtnRight);

        imgBtnFull = new ImageButton(getContext());
        imgBtnFull.setImageResource(R.drawable.ic_action_justify_full_black);
        imgBtnFull.setOnClickListener(this);
        imgBtnFull.setBackgroundColor(Color.GRAY);
        imgBtnFull.setMinimumWidth(250);
        this.horizontalLinearLayoutControl.addView(imgBtnFull);

        imgBtnUnorderedList = new ImageButton(getContext());
        imgBtnUnorderedList.setImageResource(R.drawable.ic_action_unordered_list_black);
        imgBtnUnorderedList.setOnClickListener(this);
        imgBtnUnorderedList.setBackgroundColor(Color.LTGRAY);
        imgBtnUnorderedList.setMinimumWidth(250);
        this.horizontalLinearLayoutControl.addView(imgBtnUnorderedList);

        imgBtnOrderedList = new ImageButton(getContext());
        imgBtnOrderedList.setImageResource(R.drawable.ic_action_ordered_list_black);
        imgBtnOrderedList.setOnClickListener(this);
        imgBtnOrderedList.setBackgroundColor(Color.GRAY);
        imgBtnOrderedList.setMinimumWidth(250);
        this.horizontalLinearLayoutControl.addView(imgBtnOrderedList);
    }

    @Override
    public void onClick(View v) {
        if(v == btnBold) {
            wvContent.loadUrl("javascript:document.execCommand ('bold', false, null);");
        } else if(v == btnItalic) {
            wvContent.loadUrl("javascript:document.execCommand ('italic', false, null);");
        } else if(v == btnUnderline) {
            wvContent.loadUrl("javascript:document.execCommand ('underline', false, null);");
        } else if(v == imgBtnLeft) {
            wvContent.loadUrl("javascript:document.execCommand ('justifyLeft', false, null);");
        } else if(v == imgBtnCenter) {
            wvContent.loadUrl("javascript:document.execCommand ('justifyCenter', false, null);");
        } else if(v == imgBtnRight) {
            wvContent.loadUrl("javascript:document.execCommand ('justifyRight', false, null);");
        } else if(v == imgBtnFull) {
            wvContent.loadUrl("javascript:document.execCommand ('justifyFull', false, null);");
        } else if(v == imgBtnUnorderedList) {
            wvContent.loadUrl("javascript:document.execCommand ('insertUnorderedList', false, null);");
        } else if(v == imgBtnOrderedList) {
            wvContent.loadUrl("javascript:document.execCommand ('insertOrderedList', false, null);");
        }

        wvContent.loadUrl("javascript:getWebViewContentValue();");
    }

    public void setWvContent(String wvContent) {
        this.currentContent = wvContent;
    }

    public String getValue() {
        wvContent.loadUrl("javascript:getWebViewContentValue();");
        return this.currentContent;
    }

    public void setValue(String value) {
        this.currentContent = value;
        wvContent.loadDataWithBaseURL(null, this.getHTMLDocument( value ) , "text/html", "utf-8", null);
    }

    private class MyJavaScriptInterface {
        private EditorView editor;

        public MyJavaScriptInterface(EditorView editor) {
            this.editor = editor;
        }

        @JavascriptInterface
        public void setWebViewContentValue(String html) {
            editor.setWvContent(html);
        }
    }
}
