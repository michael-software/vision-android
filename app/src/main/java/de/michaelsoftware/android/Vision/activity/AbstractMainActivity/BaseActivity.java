package de.michaelsoftware.android.Vision.activity.AbstractMainActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.view.WindowCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import net.michaelsoftware.android.jui.JuiParser;

import java.text.Normalizer;
import java.util.ArrayList;

import de.michaelsoftware.android.Vision.MyService;
import de.michaelsoftware.android.Vision.OfflineActivity;
import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.activity.MainActivity;
import de.michaelsoftware.android.Vision.service.IncomingHandlerActivity;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.HistoryHelper;
import de.michaelsoftware.android.Vision.tools.LoginHelper;
import de.michaelsoftware.android.Vision.tools.Logs;
import de.michaelsoftware.android.Vision.tools.gui.GUIHelper;
import de.michaelsoftware.android.Vision.tools.storage.OfflineHelper;
import de.michaelsoftware.android.Vision.tools.storage.SharedPreferencesHelper;

/**
 * Created by Michael on 12.05.2016.
 */
public abstract class BaseActivity extends AppCompatActivity {
    //public boolean AUTO_HIDE_ACTIONBAR = true; TODO: include
    public OfflineHelper offlineHelper;
    protected DrawerLayout mDrawerLayout;
    protected Messenger mService = null;
    protected Intent receivedIntent;
    protected GUIHelper gui;
    protected boolean mIsBound;
    protected Menu mMenu;
    final Messenger mMessenger = new Messenger(new IncomingHandlerActivity(this));
    protected LoginHelper loginHelper;
    protected HistoryHelper historyHelper;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected ListView mDrawerList;
    protected LinearLayout mDrawerTop;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected String mActivityTitle;
    protected ArrayList<String> menuArray = new ArrayList<>();
    protected ArrayList<String> menuArrayId = new ArrayList<>();

    protected Handler mHandler;
    protected Runnable searchTask;
    protected JuiParser juiParserLocal;
    private ScrollView scrollView;
    private LinearLayout linearLayout;
    public int offsetTop = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        offlineHelper = new OfflineHelper(this); // Required for all actions
        historyHelper = new HistoryHelper(this); // Required for all actions

        this.openSelectAccount(getIntent());

        //this.supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY); TODO: Include

        setupActivity();
        setupDrawer();

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openPlugin(menuArrayId.get(position));
                mDrawerLayout.closeDrawers();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        linearLayout = (LinearLayout) findViewById(R.id.refresh_layout_linear);
        scrollView = (ScrollView) findViewById(R.id.refresh_layout_scroll);

        //swipeRefreshLayout.setProgressViewOffset(true, FormatHelper.getActionBarHeight(this)/2, (int) Math.round(FormatHelper.getActionBarHeight(this)*1.5) ); TODO: include

        showActionBar();
        //offsetTop = FormatHelper.getActionBarHeight(this); TODO: include
        linearLayout.setPadding(0, offsetTop, 0, 0);

        gui = new GUIHelper(this, linearLayout, scrollView);
        juiParserLocal = new JuiParser(this, scrollView, linearLayout);

        /* TODO: include
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int historySize = motionEvent.getHistorySize();

                    if(view.getScrollY() > FormatHelper.getActionBarHeight(view.getContext())) {

                        if(historySize-1 > 0) {
                            float y = (int) motionEvent.getHistoricalY(historySize-1);

                                if ((motionEvent.getY() - y) < 0) {
                                    hideActionBar();
                                } else {
                                    showActionBar();
                                }

                        }

                    } else if(view.getScrollY() <= FormatHelper.getActionBarHeight(view.getContext())) {
                        showActionBar();
                    }
                }

                return view.onTouchEvent(motionEvent);
            }
        });*/

        super.onCreate(savedInstanceState);

        //SharedPreferencesHelper pref = new SharedPreferencesHelper(this, loginHelper.getUsername() + '@' + FormatHelper.getServerName(loginHelper.getServer()));
        //this.AUTO_HIDE_ACTIONBAR = pref.readBoolean("AUTO_HIDE_ACTIONBAR"); TODO: include
    }

    /*private void hideActionBar() { TODO: include
        if(AUTO_HIDE_ACTIONBAR) {
            if (getSupportActionBar() != null)
                getSupportActionBar().hide();
            else if (getActionBar() != null)
                getActionBar().hide();
        }
    }*/

    protected void showActionBar() {
        if (getSupportActionBar() != null)
            getSupportActionBar().show();
        else if (getActionBar() != null)
            getActionBar().show();
    }

    protected abstract void openSelectAccount(Intent intent);

    protected abstract void handleIntent(Intent intent, boolean b);

    @Override
    protected void onPause() {
        Logs.d(this, "onPause");
        if (mService != null && mService.getBinder() != null) {
            //unbindService(myConnection);
            doUnbindService();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        }
        catch (Throwable t) {
            Logs.d(this, "Failed to unbind from the service");
        }
    }

    @Override
    protected void onResume() {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this.getApplicationContext(), MyService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        am.cancel(pi);
        double minutes = 1;
        // by my own convention, minutes <= 0 means notifications are disabled
        if (minutes > 0) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    SystemClock.elapsedRealtime() + (long) minutes * 60 * 1000,
                    (long) minutes * 60 * 1000, pi);
        }

        Logs.d(this, "onResume");
        if (mService == null || mService.getBinder() == null) {
            doBindService();
        }

        super.onResume();
    }

    /* Service communication */
    void doBindService() {
        startService(new Intent(this.getApplicationContext(), MyService.class));
        bindService(new Intent(this.getApplicationContext(), MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Logs.d(this, "Service binding.");
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MyService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            Logs.d(this, "Unbinding.");
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Logs.d(this, "Service attached.");
            try {
                Message msg = Message.obtain(null, MyService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            Logs.d(this, "Service disconnected.");
        }
    };

    protected void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (MyService.isRunning()) {
            doBindService();
        }
    }

    @SuppressWarnings("unused") // maybe need it later
    private void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MyService.MSG_SET_INT_VALUE, intvaluetosend, 0);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /* End service communication */

    public LoginHelper getLoginHelper() {
        return this.loginHelper;
    }

    /* ABSTRACT */
    public abstract void openPlugin(String name);

    public abstract void openPlugin(String name, String view);

    public abstract void openPlugin(String name, String view, String parameter);

    protected abstract void getMimeContent(String data);

    protected abstract void loadMenu();

    public abstract void openHome();

    public abstract void openPluginNoHistory(String name, String view, String parameter);

    public abstract void closeRefresh();

    protected abstract void setupActivity();
    protected abstract void setupDrawer();
    public abstract void disableRefresh();

    public void sendToService(Bundle bundle, int msgActionSend) {
        Message message = Message.obtain(null, msgActionSend);
        message.setData(bundle);

        try {
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
