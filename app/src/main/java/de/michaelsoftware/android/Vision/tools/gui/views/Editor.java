package de.michaelsoftware.android.Vision.tools.gui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.HashMap;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.ThemeUtils;

/**
 * Created by Michael on 06.08.2016.
 */
public class Editor extends LinearLayout implements View.OnClickListener {
    private WebView wvContent;
    private HorizontalScrollView scrollViewControl;
    private LinearLayout horizontalLinearLayoutControl;
    private Button btnBold, btnItalic, btnUnderline;
    private ImageButton imgBtnLeft, imgBtnCenter, imgBtnRight, imgBtnFull;
    private ImageButton imgBtnUnorderedList, imgBtnOrderedList;
    private String currentContent = "";

    public Editor(Context context, HashMap<Object, Object> hashMap) {
        super(context);

        this.setOrientation(VERTICAL);

        this.setBackgroundColor(Color.argb(230, 235, 235, 235));

        wvContent = new TouchWebView(context);
        WebSettings webSettings = wvContent.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvContent.addJavascriptInterface(new MyJavaScriptInterface(this), "android");
        wvContent.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (250 * getResources().getDisplayMetrics().density)));
        wvContent.setBackgroundColor(Color.TRANSPARENT);

        this.setUpControls();

        this.addView(wvContent);

        if(hashMap.containsKey("value") && hashMap.get("value") instanceof String) {

            this.currentContent = (String) hashMap.get("value");
            wvContent.loadDataWithBaseURL(null, this.getHTMLDocument( (String) hashMap.get("value") ) , "text/html", "utf-8", null);
        } else {
            this.currentContent = (String) hashMap.get("value");
            wvContent.loadDataWithBaseURL(null, this.getHTMLDocument( (String) hashMap.get("value") ) , "text/html", "utf-8", null);
        }
    }

    private String getHTMLDocument(String value) {
        return "<html><head>" +
                "<style rel=\"stylesheet\">" +
                "body { padding: 0; margin: 0; background-color: transparent; } #content { width: 100%; padding: 15px; box-sizing: border-box; overflow: scroll;  }" +
                "</style>" +
                "</head><body><div id=\"content\" contenteditable=\"true\">" +
                FormatHelper.removeJavascript(value) + "</div><script>" +
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
        this.horizontalLinearLayoutControl.addView(btnBold);

        btnItalic = new Button(getContext());
        btnItalic.setText("I");
        btnItalic.setTypeface(null, Typeface.ITALIC);
        btnItalic.setOnClickListener(this);
        this.horizontalLinearLayoutControl.addView(btnItalic);

        btnUnderline = new Button(getContext());
        btnUnderline.setText("U");
        btnUnderline.setPaintFlags(btnUnderline.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        btnUnderline.setOnClickListener(this);
        btnUnderline.setBackgroundColor(Color.GRAY);
        this.horizontalLinearLayoutControl.addView(btnUnderline);

        imgBtnLeft = new ImageButton(getContext());
        imgBtnLeft.setImageResource(ThemeUtils.getThemeRessource(getContext().getTheme(), R.attr.justify_left));
        imgBtnLeft.setOnClickListener(this);
        this.horizontalLinearLayoutControl.addView(imgBtnLeft);

        imgBtnCenter = new ImageButton(getContext());
        imgBtnCenter.setImageResource(ThemeUtils.getThemeRessource(getContext().getTheme(), R.attr.justify_center));
        imgBtnCenter.setOnClickListener(this);
        imgBtnCenter.setBackgroundColor(Color.GRAY);
        this.horizontalLinearLayoutControl.addView(imgBtnCenter);

        imgBtnRight = new ImageButton(getContext());
        imgBtnRight.setImageResource(ThemeUtils.getThemeRessource(getContext().getTheme(), R.attr.justify_right));
        imgBtnRight.setOnClickListener(this);
        this.horizontalLinearLayoutControl.addView(imgBtnRight);

        imgBtnFull = new ImageButton(getContext());
        imgBtnFull.setImageResource(ThemeUtils.getThemeRessource(getContext().getTheme(), R.attr.justify_full));
        imgBtnFull.setOnClickListener(this);
        imgBtnFull.setBackgroundColor(Color.GRAY);
        this.horizontalLinearLayoutControl.addView(imgBtnFull);

        imgBtnUnorderedList = new ImageButton(getContext());
        imgBtnUnorderedList.setImageResource(ThemeUtils.getThemeRessource(getContext().getTheme(), R.attr.unordered_list));
        imgBtnUnorderedList.setOnClickListener(this);
        this.horizontalLinearLayoutControl.addView(imgBtnUnorderedList);

        imgBtnOrderedList = new ImageButton(getContext());
        imgBtnOrderedList.setImageResource(ThemeUtils.getThemeRessource(getContext().getTheme(), R.attr.ordered_list));
        imgBtnOrderedList.setOnClickListener(this);
        imgBtnOrderedList.setBackgroundColor(Color.GRAY);
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

    private class MyJavaScriptInterface {
        private Editor editor;

        public MyJavaScriptInterface(Editor editor) {
            this.editor = editor;
        }

        @JavascriptInterface
        public void setWebViewContentValue(String html) {
            editor.setWvContent(html);
        }
    }
}
