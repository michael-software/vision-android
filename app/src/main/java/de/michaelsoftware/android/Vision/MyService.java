package de.michaelsoftware.android.Vision;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.michaelsoftware.android.Vision.activity.MainActivity;
import de.michaelsoftware.android.Vision.service.AudioService;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.LoginHelper;
import de.michaelsoftware.android.Vision.tools.Logs;
import de.michaelsoftware.android.Vision.tools.network.WebSocketHelper;
import de.michaelsoftware.android.Vision.tools.storage.SharedPreferencesHelper;

/**
 * Service handles Notifications
 * Created by Michael on 27.02.2016.
 */
public class MyService extends AudioService {
    public static final int MSG_SET_ACTION_STRING_VALUE = 5;
    public static final String CLOSE_NOTIFICATION = PACKAGE_NAME + ".CLOSE_NOTIFICATION";
    public static final int MSG_ACTION_PLAY = 6;
    public static final int MSG_ACTION_SEND = 7;
    private static boolean isRunning = false;
    public static final int MSG_SET_INT_VALUE = 1;
    public static final int MSG_SET_STRING_VALUE = 2;
    public static final int MSG_REGISTER_CLIENT = 3;
    public static final int MSG_UNREGISTER_CLIENT = 4;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    ArrayList<Messenger> mClients = new ArrayList<>(); // Keeps track of all current registered clients.

    public List<WebSocketHelper> webSockets = new ArrayList<>();
    private ArrayList<String> notifications = new ArrayList<>();
    private ArrayList<String> notificationsText = new ArrayList<>();
    private ArrayList<Integer> notificationsNumbers = new ArrayList<>();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(MyService.CLOSE_NOTIFICATION)){
                if(intent.hasExtra("id")) {
                    int id = intent.getExtras().getInt("id");

                    if(FormatHelper.containsKey(notificationsText, id)) {
                        notificationsText.remove(id);
                    }

                    if(FormatHelper.containsKey(notificationsNumbers, id)) {
                        notificationsNumbers.remove(id);
                    }
                }
            }

            if(action.equals(MyService.ACTION_PAUSE)){
                Log.d("service", "pause");
                if(mp != null && mp.isPlaying()) {
                    mp.pause();
                    updateAudioNotification(mp.getCurrentPosition());
                } else if(mp != null) {
                    setForeground();
                    mp.playLast();
                }
            }

            if(action.equals(MyService.ACTION_STOP)){
                if(mp != null) {
                    mp.stop();
                    stopForeground(true);
                    notificationManager.cancel(musicId);
                }
            }
        }
    };

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_INT_VALUE:
                    break;
                case MSG_ACTION_PLAY:
                    if(msg.getData().containsKey("URL"))
                        playAudio(msg.getData().getString("URL"));
                    break;
                case MSG_ACTION_SEND:
                    sendJson(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }


    }

    private void sendJson(Message msg) {
        try {
            Bundle bundle = msg.getData();

            JSONObject obj = new JSONObject();

            String authkey = null;

            if(bundle.containsKey("authtoken") && bundle.getString("authtoken") != null) {
                authkey = bundle.getString("authtoken");
                obj.put("authtoken", authkey);
            }

            if(bundle.containsKey("action") && bundle.getString("action") != null) {
                obj.put("action", bundle.getString("action"));
            }

            if(bundle.containsKey("value") && bundle.getString("value") != null) {
                obj.put("value", bundle.getString("value"));
            }

            if(bundle.containsKey("plugin") && bundle.getString("plugin") != null) {
                obj.put("plugin", bundle.getString("plugin"));
            }

            if(authkey != null) {
                for (WebSocketHelper webSocketHelper: webSockets) {
                    if(webSocketHelper != null && webSocketHelper.getAuthkey() != null && webSocketHelper.getAuthkey().equals(authkey)) {
                        webSocketHelper.sendMessage(obj.toString());
                    }
                }
            }

            Log.d("JSON", obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Logs.d("Service started");

        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String accountType = this.getResources().getString(R.string.account_type);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MyService.CLOSE_NOTIFICATION); //further more
        filter.addAction(MyService.ACTION_PAUSE);
        filter.addAction(MyService.ACTION_STOP);
        registerReceiver(receiver, filter);

        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(accountType);

        if (accounts != null && accounts.length > 0 && accounts[0] != null) {
            for (final Account account : accounts) {
                final String userName = LoginHelper.getUsernameFromAccountName(account.name);
                final String serverName = FormatHelper.getServerUrl(LoginHelper.getServerNameFromAccountName(account.name));
                final MyService _this = this;

                accountManager.getAuthToken(account, "login", null, false, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            String authtoken = future.getResult().getString("authtoken");


                            SharedPreferencesHelper pref = new SharedPreferencesHelper(_this, userName + '@' + FormatHelper.getServerName(serverName));
                            String host = pref.read("HOST", null);
                            String wsport = pref.read("WSPORT", null);
                            String activated = pref.read("NOTIFICATION", "1");

                            Logs.v(this, host + ":" + wsport);
                            if(!activated.equals("0") && host != null && !host.equals("") && wsport != null && !wsport.equals("")) {
                                WebSocketHelper wsh = new WebSocketHelper(_this, account);
                                WebSocket ws = wsh.execute(host, wsport, authtoken);

                                if(wsh != null && wsh.isOpen())
                                    addWebSocket(wsh);
                            }
                        } catch (OperationCanceledException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (AuthenticatorException e) {
                            e.printStackTrace();
                        }

                    }
                }, null);
            }
        }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (webSockets == null || webSockets.size() == 0) {
                            Logs.d(this, "close Websockets");
                            Logs.d(this, webSockets.size() + "");
                            stopSelf();
                        }
                    }
                }, 10000);

        this.startReconnect();

        isRunning = true;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);

        super.onDestroy();
        Logs.d(this, "Service Stopped.");
        isRunning = false;
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    private void sendMessageToUI(String value) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                Bundle b = new Bundle();
                b.putString("action", value);
                Message msg = Message.obtain(null, MSG_SET_ACTION_STRING_VALUE);
                msg.setData(b);
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void startReconnect() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        reconnect();
                    }
                }, 5*60*1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public void addWebSocket(WebSocketHelper webSocket) {
        webSockets.add(webSocket);
    }

    public void notifyUser(String title, String text, Account account, String action, String plugin, String icon) {
        android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);

        int mId = id;
        if(!plugin.equals("") && !plugin.equals("server")) {
            if (!notifications.contains(plugin + " (" + account.name + ")"))
                notifications.add(plugin + " (" + account.name + ")");

            mId = notifications.indexOf(plugin + " (" + account.name + ")");
            if (!FormatHelper.containsKey(notificationsText, mId)) {
                notificationsText.add(mId, text);
            } else {
                String oldText = notificationsText.get(mId);
                text = oldText + "\n" + text;
                notificationsText.remove(mId);
                notificationsText.add(mId, text);
            }

            if (!FormatHelper.containsKey(notificationsNumbers, mId)) {
                notificationsNumbers.add(mId, 1);
            } else {
                int count = notificationsNumbers.get(mId);
                count++;
                mBuilder.setNumber(count);
                notificationsNumbers.remove(mId);
                notificationsNumbers.add(mId, count);

                mBuilder.setContentTitle("Vision - " + plugin);
                mBuilder.setContentText(count + " Benachrichtigungen");
            }
        }

        if(plugin.equals("server")) {
            mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        }

        mBuilder.setLights(Color.RED, 3000, 3000);

        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        mBuilder.setSubText(account.name);

        if(icon != null && !icon.equals("")) {
            mBuilder.setSmallIcon(R.drawable.vision_icon);

            Bitmap bitmap = FormatHelper.baseToBitmap(icon, 192, 192);

            mBuilder.setLargeIcon(bitmap);
        } else {
            mBuilder.setSmallIcon(R.drawable.vision_icon);
        }

        mBuilder.setColor(Color.RED);

        mBuilder.setAutoCancel(true);

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("account", account);
        resultIntent.putExtra("action", action);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        id,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);


        Intent intent = new Intent(MyService.CLOSE_NOTIFICATION);
        intent.putExtra("id", mId);
        intent.setAction(MyService.CLOSE_NOTIFICATION);
        PendingIntent deleteIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);
        mBuilder.setDeleteIntent(deleteIntent);

        // mId allows you to update the notification later on.
        notificationManager.notify(mId, mBuilder.build());
        id++;
    }

    public void notifyUser(String title, String text, Account account, String action, String plugin) {
        this.notifyUser(title, text, account, action, plugin, "");
    }

    public void sendAction(Account account, String action) {
        Logs.d(this, action);

        this.sendMessageToUI(action);
    }

    public void reconnect() {
        String accountType = this.getResources().getString(R.string.account_type);

        Logs.v(this, "reconnect");

        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(accountType);

        if (accounts != null && accounts.length > 0 && accounts[0] != null) {
            for (final Account account : accounts) {
                final String userName = LoginHelper.getUsernameFromAccountName(account.name);
                final String serverName = FormatHelper.getServerUrl(LoginHelper.getServerNameFromAccountName(account.name));
                final MyService _this = this;

                accountManager.getAuthToken(account, "login", null, false, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            String authtoken = future.getResult().getString("authtoken");


                            SharedPreferencesHelper pref = new SharedPreferencesHelper(_this, userName + '@' + FormatHelper.getServerName(serverName));
                            String host = pref.read("HOST", null);
                            String wsport = pref.read("WSPORT", null);
                            String activated = pref.read("NOTIFICATION", "1");

                            boolean skip = false;
                            ArrayList<WebSocketHelper> removeObjects = new ArrayList<>();
                            for(WebSocketHelper wshelper : webSockets) {
                                if(wshelper.getHost() != null && wshelper.getHost().equals(host) && wshelper.getAuthkey() != null && wshelper.getAuthkey().equals(authtoken)) {
                                    skip = true;

                                    if(!activated.equals("0")) {
                                        removeObjects.add(wshelper);
                                    }
                                } else if(wshelper.getHost() == null) {
                                    removeObjects.add(wshelper);
                                }
                            }

                            for (WebSocketHelper id : removeObjects){
                                webSockets.remove(id);
                            }
                            if(!skip) {

                                Logs.v(this, host + ":" + wsport);

                                if (!activated.equals("0") && host != null && !host.equals("") && wsport != null && !wsport.equals("")) {
                                    WebSocketHelper wsh = new WebSocketHelper(_this, account);
                                    WebSocket ws = wsh.execute(host, wsport, authtoken);

                                    if (wsh != null && wsh.isOpen())
                                        addWebSocket(wsh);
                                }
                            }
                        } catch (OperationCanceledException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (AuthenticatorException e) {
                            e.printStackTrace();
                        }

                    }
                }, null);
            }
        }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if(webSockets == null || webSockets.size() == 0) {
                            Logs.v(this, "close Websockets");
                            Logs.v(this, webSockets.size()+"");
                            stopSelf();
                        }
                    }
                }, 10000);

        this.startReconnect();
    }
}
