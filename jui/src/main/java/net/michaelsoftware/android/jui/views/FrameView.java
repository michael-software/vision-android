package net.michaelsoftware.android.jui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import net.michaelsoftware.android.jui.JuiParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Michael on 06.09.2016.
 */
public class FrameView extends WebView {
    private TypedValue typedValue;
    private String url;
    private String html;

    public FrameView(JuiParser juiParser) {
        super(juiParser.getActivity());

        this.setSettings(juiParser, true);
    }

    public FrameView(JuiParser juiParser, boolean secureMode) {
        super(juiParser.getActivity());

        this.setSettings(juiParser, secureMode);

        this.typedValue = new TypedValue();
    }

    public void setUrl(String url) {
        this.loadUrl(url);
    }

    public void setHtml(String html) {
        this.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
    }


    @SuppressLint("NewApi")
    public void setSettings(JuiParser juiParser, boolean secureMode) {
        WebSettings webSettings = this.getSettings();

        if(!secureMode && Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setJavaScriptEnabled(true);
        }

        this.setVerticalScrollBarEnabled(true);
        this.setHorizontalScrollBarEnabled(true);

        webSettings.setAllowContentAccess(true);
        this.setWebViewClient(new WebViewClient());

        this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }

        webSettings.setAllowFileAccess(false);
        webSettings.setPluginState(WebSettings.PluginState.OFF);

        juiParser.requestUnfreshable();

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ECLAIR) {
            try {
                Method m1 = WebSettings.class.getMethod("setDomStorageEnabled", Boolean.TYPE);
                m1.invoke(webSettings, Boolean.TRUE);

                Method m2 = WebSettings.class.getMethod("setDatabaseEnabled", Boolean.TYPE);
                m2.invoke(webSettings, Boolean.TRUE);

                Method m3 = WebSettings.class.getMethod("setDatabasePath", String.class);
                m3.invoke(webSettings, getContext().getApplicationContext().getFilesDir().getPath() + getContext().getPackageName() + "/databases/");

                Method m4 = WebSettings.class.getMethod("setAppCacheMaxSize", Long.TYPE);
                m4.invoke(webSettings, 1024*1024*8);

                Method m5 = WebSettings.class.getMethod("setAppCachePath", String.class);
                m5.invoke(webSettings, getContext().getApplicationContext().getFilesDir().getPath() + getContext().getPackageName() + "/cache/");

                Method m6 = WebSettings.class.getMethod("setAppCacheEnabled", Boolean.TYPE);
                m6.invoke(webSettings, Boolean.TRUE);
            } catch (NoSuchMethodException e) {
                Log.d("FrameView-NoSuchMethod", e.getMessage());
            } catch(InvocationTargetException e) {
                Log.d("FrameView-Invocation", e.getMessage());
            } catch (IllegalAccessException e) {
                Log.d("FrameView-IllegalAccess", e.getMessage());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int historySize = event.getHistorySize();
            if(historySize > 0) {
                int y = (int) event.getHistoricalY(historySize-1);

                if (this.getScrollY() > 0 || (event.getY() - y) < 0) {
                    requestDisallowInterceptTouchEvent(true);
                } else {
                    requestDisallowInterceptTouchEvent(false);
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;

        int resource = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        int actionBarHeight = 0;
        int statusBarHeight = 0;

        if (resource > 0) {
            statusBarHeight = getContext().getResources().getDimensionPixelSize(resource);
        }

        if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, this.typedValue, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(this.typedValue.data,getContext().getResources().getDisplayMetrics());
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec((screenHeight - statusBarHeight - actionBarHeight*2), MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
