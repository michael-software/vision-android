package de.michaelsoftware.android.Vision.activity.AbstractMainActivity;

import android.accounts.Account;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.util.HashMap;

import de.michaelsoftware.android.Vision.listener.OnAuthtokenGetListener;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.gui.GUIHelper;
import de.michaelsoftware.android.Vision.tools.gui.MethodHelper;
import de.michaelsoftware.android.Vision.tools.network.HttpPostJsonHelper;

/**
 * Created by Michael on 12.05.2016.
 */
public abstract class HandleIntentActivity extends BaseActivity {
    public static final int GET_USERDATA = 7564; // http://codebeautify.org/string-hex-converter (userdata = ud = 7564)
    //public static final int SET_USER = 7375; // http://codebeautify.org/string-hex-converter (set user = su = 7375)
    public static final int ADD_USER = 1;
    public static final int SELECT_USER = 7376; // http://codebeautify.org/string-hex-converter (set user = su = 7375 + 1)
    public boolean isOpeningPlugin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        loginHelper.setOnAuthtokenGetListener(new OnAuthtokenGetListener() { // Handle Intent after Got Authtoken (Full-Login) from the LoginHelper
            @Override
            public void onAuthtokenGet(String authToken) {
                handleIntent(intent, false);
                loadMenu();
            }
        });
    }

    /* Handle intents */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_USER) {
            loginHelper.openSelectUserAccount();
        }

        if (requestCode == SELECT_USER) { // Check which request we're responding to
            if (resultCode == Activity.RESULT_OK) { // Make sure the request was successful
                this.selectAccount(data);

                if(data.hasExtra("intent")) {
                    Intent parcelableIntent = (Intent) data.getExtras().get("intent");

                    if (parcelableIntent != null && Intent.ACTION_SEND.equals(parcelableIntent.getAction())) {
                        String receivedType = parcelableIntent.getType();

                        if (receivedType != null && !receivedType.equals("")) {
                            //Uri receivedUri = parcelableIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                            this.receivedIntent = parcelableIntent;

                            String urlStr = loginHelper.getServer() + "ajax.php?show=plugins";
                            String dataString = offlineHelper.getData(urlStr);

                            if(dataString.equals("")) {
                                offlineHelper.downloadOfflineData(urlStr);

                                HttpPostJsonHelper httpPost = new HttpPostJsonHelper(loginHelper);
                                httpPost.setOutput(this, "getMimeContent");
                                httpPost.execute(urlStr);
                            } else {
                                this.getMimeContent(dataString);
                            }
                        }
                    }

                    if(parcelableIntent != null) {
                        this.handleActionIntent(parcelableIntent);
                    }

                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //loginHelper = new LoginHelper(this);

                if(data != null && data.hasExtra("intent")) {
                    loginHelper.openSelectUserAccount( (Intent) data.getExtras().get("intent") );
                } else {
                    loginHelper.openSelectUserAccount();
                }
            }
        } else if(requestCode == GUIHelper.REQUEST_FILE) {
            /*
            if(data != null) {
                String path = data.getDataString();

                if (gui.getCurrentView() != null && gui.getCurrentView() instanceof UploadButton && path != null) {
                    ((UploadButton) gui.getCurrentView()).setFile(path);

                    gui.setCurrentView(null);
                }
            }*/
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LoginHelper.OPEN_SELECT_USERS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.loginHelper.openSelectUserAccount();
                } else {
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent, false);
    }
    /* End Handle Intents */

    public void handleIntent(final Intent intent, boolean startup) {
        if (intent.hasExtra("account")) {
            Account account = intent.getParcelableExtra("account");

            //loginHelper = new LoginHelper(this); /* TODO */
            loginHelper.selectUserAccount(account);

            this.loadMenu();
        } else if (intent.hasExtra("user")) {
            String user   = intent.getStringExtra("user");

            //loginHelper = new LoginHelper(this);
            Account account = loginHelper.getAccount(user);

            if(account != null) {
                loginHelper.selectUserAccount(account);

                handleActionIntent(intent);

                this.loadMenu();
            } else {
                loginHelper.openSelectUserAccount();
            }
        }

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mHandler.removeCallbacks(searchTask);

            String query = intent.getStringExtra(SearchManager.QUERY);

            HashMap<String, String> list = new HashMap<>();
            list.put("search", query);

            if(this.loginHelper != null) {
                HttpPostJsonHelper postHelper = new HttpPostJsonHelper(this.loginHelper);
                postHelper.setSpecialData(query);
                postHelper.setOutput(this, "insertSearch");
                postHelper.setPost(list);
                postHelper.execute(loginHelper.getServer() + "ajax.php?action=search");
            }
        } else if (Intent.ACTION_SEND.equals(intent.getAction()) && loginHelper != null) {
            /*if(!startup) {
                loginHelper.openSelectUserAccount(intent);
            }*/ /* TODO */

            String receivedType = intent.getType();

            if (receivedType != null && !receivedType.equals("")) {
                //Uri receivedUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                this.receivedIntent = intent;

                String urlStr = loginHelper.getServer() + "ajax.php?show=plugins";
                String dataString = offlineHelper.getData(urlStr);

                if(dataString.equals("")) {
                    offlineHelper.downloadOfflineData(urlStr);

                    HttpPostJsonHelper httpPost = new HttpPostJsonHelper(loginHelper);
                    httpPost.setOutput(this, "getMimeContent");
                    httpPost.execute(urlStr);
                } else {
                    this.getMimeContent(dataString);
                }
            }

        } else if (intent.hasExtra("action")) {
            final BaseActivity _this = this;

            this.loginHelper.setOnAuthtokenGetListener(new OnAuthtokenGetListener() {
                @Override
                public void onAuthtokenGet(String authToken) {
                    String action = intent.getExtras().getString("action");

                    MethodHelper methodHelper = new MethodHelper();
                    methodHelper.call(action, _this);
                }
            });

        } else if(!startup) {
            this.handleActionIntent(intent);
        }
    }

    private void handleActionIntent(Intent intent) {
        if(Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            String action = intent.getAction();
            Uri data = intent.getData();

            String plugin = FormatHelper.getPluginFromUri(data);

            if(plugin != null) {
                this.openPlugin(plugin);
            }
        } else if (!intent.hasExtra("action") && !Intent.ACTION_SEND.equals(intent.getAction())) {
            this.openHome();
        }
    }

    /* ABSTRACT */
    protected abstract void selectAccount(Intent data);
}
