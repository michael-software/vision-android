package de.michaelsoftware.android.Vision.tools.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.interfaces.Listener;
import net.michaelsoftware.android.jui.models.NameValue;
import net.michaelsoftware.android.jui.models.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.activity.AbstractMainActivity.BaseActivity;
import de.michaelsoftware.android.Vision.activity.MainActivity;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.LoginHelper;
import de.michaelsoftware.android.Vision.tools.Logs;
import de.michaelsoftware.android.Vision.tools.ResourceHelper;
import de.michaelsoftware.android.Vision.tools.SecurityHelper;
import de.michaelsoftware.android.Vision.tools.ThemeUtils;
import de.michaelsoftware.android.Vision.tools.gui.listener.OnSwipeTouchListener;
import de.michaelsoftware.android.Vision.tools.gui.views.ButtonList;
import de.michaelsoftware.android.Vision.tools.gui.views.Editor;
import de.michaelsoftware.android.Vision.tools.gui.views.EditorView;

/**
 * Created by Michael on 29.11.2015.
 * Free non-commercial use and modify of the source code
 */
public class GUIHelper implements Listener.OnParseHeaderListener, Listener.OnBeforeParseListener {
    private LinearLayout linear;
    private ScrollView scroll;
    private BaseActivity mainActivity;
    public AlertDialog alertDialog;

    private JuiParser juiParser;

    private HashMap<Object, Object> juiHead;

    public int padding       = 30;
    public int paddingTop    = padding;
    public int paddingLeft   = padding;
    public int paddingRight  = padding;
    public int paddingBottom = padding;

    private String swipeTop = "", swipeLeft = "", swipeRight = "", swipeBottom = "";

    public static final int REQUEST_FILE = 2534;

    HashMap<String, Object> editElements = new HashMap<>();
    HashMap<String, Object> idElements = new HashMap<>();

    public static String sep1 = "%!#|params|#!%";
    private View currentView;

    public GUIHelper(BaseActivity main, LinearLayout pLinear, ScrollView pScroll) {
        this.mainActivity = main;
        this.linear = pLinear;
        this.scroll = pScroll;

        juiParser = new JuiParser(main, this.scroll, this.linear);
        juiParser.addAction("openPlugin", 1, mainActivity);
        juiParser.addAction("openPlugin", 2, mainActivity);
        juiParser.addAction("openPlugin", 3, mainActivity);
        juiParser.addAction("openMedia", 2, mainActivity);
        juiParser.addAction("openGallery", 2, mainActivity);
        juiParser.addAction("sendAsync", 2, mainActivity);

        juiParser.addElement(new ViewModel("editor", "ed", Editor.class));
        juiParser.addElement(new ViewModel("buttonlist", "btl", ButtonList.class));
        juiParser.setOnSubmitListener(new Listener.OnSubmitListener() {

            @Override
            public String onSubmit(View view) {
                if(view instanceof EditorView) {
                    return ((EditorView) view).getValue();
                }

                return null;
            }
        });

        juiParser.setOnParseHeaderListener(this);
        juiParser.setOnBeforeParse(this);
    }

    public void clear() {
        linear.removeAllViews();
        this.editElements.clear();
        this.scroll.setBackgroundColor(Color.TRANSPARENT);

        this.swipeTop = "";
        this.swipeLeft = "";
        this.swipeRight = "";
        this.swipeBottom = "";
        //this.scroll.setOnTouchListener(null);
        if(mainActivity instanceof MainActivity) {
            ((MainActivity) mainActivity).enableRefresh();
        }

        this.scroll.scrollTo(0, 0);
    }

    @SuppressWarnings("unchecked")
    public void parse(HashMap<Object, Object> hashMap) { /* TODO: Parse Head in JuiParser */
        juiParser.parse(hashMap);
    }

    public Activity getActivity() {
        return mainActivity;
    }

    public void setCurrentView(View currentView) {
        this.currentView = currentView;
    }

    public View getCurrentView() {
        return currentView;
    }

    public LinearLayout.LayoutParams getLayoutParams() {
        return (LinearLayout.LayoutParams) linear.getLayoutParams();
    }

    public void toggleView(String id) {
        if(idElements.containsKey(id) && idElements.get(id) instanceof View) {
            View view = (View) idElements.get(id);

            if(view.getVisibility() == View.VISIBLE) {
                view.setVisibility(View.GONE);
            } else if(view.getVisibility() == View.GONE) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    public void parse(String urlStr) {
        juiParser.setCustomHttpHeader( "Authorization", "bearer " + mainActivity.getLoginHelper().getAuthtoken() );
        juiParser.parseUrl(urlStr);
    }

    public HashMap<Object, Object> getHeader() {
        return this.juiHead;
    }

    @Override
    public void onParseHead(HashMap<Object, Object> hashMap) {
        this.juiHead = hashMap;

        if(hashMap.containsKey("status") && hashMap.get("status") instanceof Integer) {
            int status = (int) hashMap.get("status");
            if(status == 401) {
                LoginHelper.deleteAccount(mainActivity, mainActivity.getLoginHelper().getAccount());
                mainActivity.getLoginHelper().openSelectUserAccount();
            }
        }

        if(hashMap.containsKey("jwt") && hashMap.get("jwt") instanceof String) {
            String newJwt = (String) hashMap.get("jwt");
            if(!newJwt.equals("")) {
                mainActivity.getLoginHelper().setNewAuthtoken(newJwt);
            }
        }

        Object valueBackgroundColor = hashMap.get("bgcolor");
        if (valueBackgroundColor != null && valueBackgroundColor instanceof String) {
            this.scroll.setBackgroundColor(FormatHelper.parseColor((String) valueBackgroundColor));
        } else {
            this.scroll.setBackgroundColor(Color.TRANSPARENT);
        }

        if(hashMap.containsKey("refreshable") && hashMap.get("refreshable") instanceof String && ((String) hashMap.get("refreshable")).equalsIgnoreCase("FALSE")) {
            if(mainActivity instanceof MainActivity) {
                ((MainActivity) mainActivity).disableRefresh();
            }
        }

        if(hashMap.containsKey("scroll") && hashMap.get("scroll") instanceof String && ((String) hashMap.get("scroll")).equalsIgnoreCase("BOTTOM")) {
            scroll.post(new Runnable() {
                @Override
                public void run() {
                    scroll.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

        if(hashMap.containsKey("share") && hashMap.get("share") instanceof HashMap && mainActivity instanceof MainActivity) {
            HashMap<Object, Object> share = (HashMap<Object, Object>) hashMap.get("share");

            if(share.containsKey("name") && share.get("name") instanceof String) {
                String name = (String) share.get("name");

                String page = "home";
                if(share.containsKey("view") && share.get("view") instanceof String) {
                    page = (String) share.get("view");
                }

                String command = "";
                if(share.containsKey("command") && share.get("command") instanceof String) {
                    command = (String) share.get("command");
                }

                ((MainActivity) mainActivity).setShareable(name, page, command);
            } else {
                ((MainActivity) mainActivity).setShareable(false);
            }
        } else if(mainActivity instanceof MainActivity) {
            ((MainActivity) mainActivity).setShareable(false);
        }

        Object swipeTopAction = hashMap.get("swipetop");
        Object swipeLeftAction = hashMap.get("swipeleft");
        Object swipeRightAction = hashMap.get("swiperight");
        Object swipeBottomAction = hashMap.get("swipebottom");

        if(swipeTopAction != null || swipeLeftAction != null || swipeRightAction != null || swipeBottomAction != null) {

            if(swipeTopAction != null && swipeTopAction instanceof String) {
                this.swipeTop = (String) swipeTopAction;
            }

            if(swipeLeftAction != null && swipeLeftAction instanceof String) {
                this.swipeLeft = (String) swipeLeftAction;
            }

            if(swipeRightAction != null && swipeRightAction instanceof String) {
                this.swipeRight = (String) swipeRightAction;
            }

            if(swipeBottomAction != null && swipeBottomAction instanceof String) {
                if(mainActivity instanceof MainActivity) {
                    ((MainActivity) mainActivity).disableRefresh();
                }

                this.swipeBottom = (String) swipeBottomAction;
            }

            scroll.setOnTouchListener(new OnSwipeTouchListener(mainActivity) {
                public void onSwipeTop() {
                    MethodHelper methodHelper = new MethodHelper(false);
                    methodHelper.call(swipeTop, mainActivity);

                    Logs.toast(mainActivity, "top", Toast.LENGTH_LONG);
                }

                public void onSwipeRight() {
                    MethodHelper methodHelper = new MethodHelper(false);
                    methodHelper.call(swipeRight, mainActivity);

                    Logs.toast(mainActivity, "right", Toast.LENGTH_LONG);
                }

                public void onSwipeLeft() {
                    MethodHelper methodHelper = new MethodHelper(false);
                    methodHelper.call(swipeLeft, mainActivity);

                    Logs.toast(mainActivity, "left", Toast.LENGTH_LONG);
                }

                public void onSwipeBottom() {
                    MethodHelper methodHelper = new MethodHelper(false);
                    methodHelper.call(swipeBottom, mainActivity);

                    Logs.toast(mainActivity, "bottom", Toast.LENGTH_LONG);
                }

            });
        }
    }

    @Override
    public boolean onBeforeParse(HashMap<Object, Object> hashMap) {
        mainActivity.closeRefresh();

        linear.setPadding(paddingLeft, paddingTop+mainActivity.offsetTop, paddingRight, paddingBottom);

        if(hashMap != null && hashMap.containsKey("type") && hashMap.get("type") instanceof String
                && ((String) hashMap.get("type")).equalsIgnoreCase("flyover")
                && hashMap.containsKey("value") && hashMap.get("value") instanceof HashMap) {

            HashMap<Object, Object> hashMapValue = (HashMap<Object, Object>) hashMap.get("value");
            LinearLayout view = new LinearLayout(mainActivity);
            view.setOrientation(LinearLayout.VERTICAL);
            view.setPadding((int) ResourceHelper.getDimen(mainActivity, R.dimen.activity_horizontal_margin),
                    (int) ResourceHelper.getDimen(mainActivity, R.dimen.activity_vertical_margin),
                    (int) ResourceHelper.getDimen(mainActivity, R.dimen.activity_horizontal_margin),
                    (int) ResourceHelper.getDimen(mainActivity, R.dimen.activity_vertical_margin));

            view.addView(juiParser.parseReturn(hashMapValue, true));

            AlertDialog.Builder alertDialogBuilder;
            if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
                alertDialogBuilder = new AlertDialog.Builder(mainActivity, R.style.DialogDark);
            } else {
                alertDialogBuilder = new AlertDialog.Builder(mainActivity, R.style.DialogLight);
            }

            ScrollView scrollView = new ScrollView(mainActivity);
            scrollView.addView(view);
            alertDialogBuilder.setView(scrollView);
            alertDialogBuilder.setCancelable(true);
            this.alertDialog = alertDialogBuilder.create();

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(this.alertDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            try {
                this.alertDialog.show();
                this.alertDialog.getWindow().setAttributes(lp);
            } catch (Exception e) {
                // WindowManager$BadTokenException will be caught and the app would
                // not display the 'Force Close' message
                e.printStackTrace();
            }

            return false;
        } else if(hashMap != null && hashMap.containsKey("redirect") && hashMap.get("redirect") instanceof HashMap && mainActivity instanceof MainActivity) {
            HashMap hm = (HashMap) hashMap.get("redirect");
            String s1 = (String) hm.get(0);
            String s2 = (String) hm.get(1);
            String s3 = (String) hm.get(2);
            mainActivity.openPlugin(s1, s2, s3);
            ((MainActivity) mainActivity).historyRemoveLast();

            return false;
        } else if(hashMap != null) {
            return true;
        }

        return false;
    }

    public void reload() {
        juiParser.reload();
    }
}
