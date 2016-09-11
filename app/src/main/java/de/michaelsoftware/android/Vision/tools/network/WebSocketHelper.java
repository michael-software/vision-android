package de.michaelsoftware.android.Vision.tools.network;

import android.accounts.Account;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.michaelsoftware.android.Vision.MyService;
import de.michaelsoftware.android.Vision.tools.Logs;

/**
 * Created by Michael on 28.02.2016.
 * An Helper class for managing WebSocket connections to the server
 */
public class WebSocketHelper {
    private final MyService myService;
    private final Account account;
    private WebSocket ws;
    private String server;
    private String authkey;

    public WebSocketHelper(MyService pMyService, Account pAccount) {
        this.myService = pMyService;
        this.account = pAccount;
    }

    public WebSocket execute(String... params) {
        if(params.length != 3) {
            return null;
        }

        this.server  = params[0];
        String port    = params[1];
        this.authkey = params[2];

        Logs.v(this, server + ":" + port);

        try {
            WebSocketFactory factory = new WebSocketFactory();
            factory.setConnectionTimeout(5000);
            this.ws = factory.createSocket("ws://" + server + ":" + port);
            ws.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);

            ws.setFrameQueueSize(5);
            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                    websocket.sendText("login "+authkey);
                    onPostExecute(websocket);
                    Logs.d(this, "connected WebSocket");
                }

                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    if(message != null && !message.equals("") && !message.equals("")) {
                        Log.d("WebSocket Message", message);
                        JsonParser jsonParser = new JsonParser(message);
                        HashMap hashMap = jsonParser.getHashMap();

                        if(!hashMap.containsKey("type") || hashMap.get("type").equals("notification")) {
                            if (hashMap.containsKey("title") && hashMap.get("title") instanceof String
                                    && hashMap.containsKey("text") && hashMap.get("text") instanceof String) {

                                String title = (String) hashMap.get("title");
                                String text = (String) hashMap.get("text");
                                String action = (String) hashMap.get("action");

                                String plugin = "";
                                if(hashMap.containsKey("plugin") && hashMap.get("plugin") instanceof String) {
                                    plugin = (String) hashMap.get("plugin");
                                }

                                if(hashMap.containsKey("server") && hashMap.get("server") instanceof String && hashMap.get("server").equals("1")) {
                                    plugin = "server";
                                }

                                if(hashMap.containsKey("icon") && hashMap.get("icon") instanceof String) {
                                    myService.notifyUser(title, text, account, action, plugin, (String) hashMap.get("icon"));
                                } else {
                                    myService.notifyUser(title, text, account, action, plugin);
                                }

                            }
                        } else if(hashMap.get("type").equals("action")) {
                            if (hashMap.containsKey("action") && hashMap.get("action") instanceof String) {
                                String action = (String) hashMap.get("action");
                                myService.sendAction(account, action);
                            }
                        }
                    }
                }

                @Override
                public void onConnectError(WebSocket websocket, WebSocketException cause) {
                    Logs.v(this, cause.toString());
                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) {
                    Logs.v(this, cause.toString());
                }
            });

            ws.connectAsynchronously();

            if(ws.isOpen()) {
                Logs.v(this, authkey);
                ws.sendText("login "+authkey);
                return ws;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void sendMessage(String message) {
        if(ws != null && ws.isOpen()) {
            ws.sendText(message);
        }
    }

    public boolean isOpen() {
        return ws.isOpen();
    }

    protected void onPostExecute(WebSocket result) {
        if(myService != null && result != null && result.isOpen()) {
            myService.addWebSocket(this);
        }
    }

    public String getHost() {
        if(ws.isOpen()) {
            return this.server;
        }

        return null;
    }

    public String getAuthkey() {
        if(ws.isOpen()) {
            return this.authkey;
        }

        return null;
    }
}
