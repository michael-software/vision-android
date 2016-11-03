package de.michaelsoftware.android.Vision.tools;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import net.michaelsoftware.android.jui.network.HttpPostJsonHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.michaelsoftware.android.Vision.OfflineActivity;
import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.account.AccountGeneral;
import de.michaelsoftware.android.Vision.activity.AddNewAccountActivity;
import de.michaelsoftware.android.Vision.activity.LoginSelectActivity;
import de.michaelsoftware.android.Vision.activity.MainActivity;
import de.michaelsoftware.android.Vision.activity.StartServerActivity;
import de.michaelsoftware.android.Vision.listener.OnAuthtokenGetListener;
import de.michaelsoftware.android.Vision.tools.storage.SharedPreferencesHelper;

/**
 * Created by Michael on 06.12.2015.
 * class manages all Login Events and gives information over the current user
 * (e.g. Server-Url, Username, Authtoken)
 */
public class LoginHelper {
    public static final int OPEN_SELECT_USERS = 1;
    private Activity activity;
    private AccountManager accountManager;
    private String accountType;
    private List<OnAuthtokenGetListener> authtokenGetListeners = new ArrayList<>();

    private String AUTHTOKEN;
    private String SERVER;
    private String USERNAME;
    private String USERNAME_TMP = "tmp";
    private Account CURRENT_ACCOUNT;

    private Intent intent;
    public boolean seamlessActivated = true;

    public LoginHelper(Activity pActivity) {
        activity = pActivity;
        accountType = activity.getResources().getString(R.string.account_type);
        accountManager = AccountManager.get(activity);
    }

    public static void getUserData(String pServer, String pUsername, String pPassword, Object c, String m) {
        String key = SecurityHelper.generateKey();
        String iv = SecurityHelper.generateKey(16);

        HashMap<String, String> nameValuePair = new HashMap<>();

        nameValuePair.put("username", SecurityHelper.encrypt(pUsername, key, iv));
        nameValuePair.put("password", SecurityHelper.encrypt(pPassword, key, iv));

        //nameValuePair.put("username", pUsername);
        //nameValuePair.put("password", pPassword);

        nameValuePair.put("key", SecurityHelper.encrypt(key));
        nameValuePair.put("iv", SecurityHelper.encrypt(iv, key));

        HttpPostJsonHelper httpPost = new HttpPostJsonHelper();
        httpPost.setKeyIv(key, iv);
        httpPost.setOutputString(true);
        httpPost.setOutput(c, m);
        httpPost.setPost(nameValuePair);
        httpPost.execute(pServer);
    }

    public void setNewAuthtoken(String newAuthtoken) {
        this.AUTHTOKEN = newAuthtoken;

        AccountManager accountManager = AccountManager.get(this.activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.setAuthToken(this.getAccount(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, newAuthtoken);
        }
    }

    private boolean loginUser(String pServer, Account pAccount, String authtoken) {
        Logs.v(this, "Change username");
        USERNAME = LoginHelper.getUsernameFromAccountName(pAccount.name);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + authtoken);

        HttpPostJsonHelper httpPost = new HttpPostJsonHelper();
        httpPost.setOutput(this, "getUserDataLogin");
        httpPost.setHeaders(headers);
        httpPost.execute(pServer);

        return false;
    }

    public static boolean isLoggedIn(HashMap<Object, Object> hashMap, String key, String iv) {
        if (hashMap.containsKey("status") && hashMap.get("status") instanceof String) {
            String status = SecurityHelper.decrypt((String) hashMap.get("status"), key, iv);

            if (status != null && status.equals("login")) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused") // used by invoke from HttpPostJson
    public void getUserDataLogin(HashMap<Object, Object> hashMap) {
        if (hashMap == null) {
            SharedPreferencesHelper pref = new SharedPreferencesHelper(activity, this.getIdentifier());
            String mac = pref.read("MAC");
            String wolserver = pref.read("WOLSERVER");

            Intent intent = new Intent(activity, StartServerActivity.class);
            intent.putExtra("IP", FormatHelper.getServerName(SERVER));
            intent.putExtra("MAC", mac);
            intent.putExtra("WOLSERVER", wolserver);
            intent.putExtra("AUTHTOKEN", this.AUTHTOKEN);

            activity.startActivity(intent);
        } else {
            String status = (String) hashMap.get("status");
            String mac = (String) hashMap.get("mac");
            String wolserver = (String) hashMap.get("wolserver");
            String host = (String) hashMap.get("host");

            String wsport = "";
            if (hashMap.containsKey("wsport")) {
                wsport = (String) hashMap.get("wsport");
            }

            String mainPlugins = "";
            if (hashMap.containsKey("mainplugins")) {
                mainPlugins = (String) hashMap.get("mainplugins");
            }

            if (status != null && status.equals("login")) {
                if (mac != null) {
                    SharedPreferencesHelper pref = new SharedPreferencesHelper(activity, this.USERNAME + '@' + FormatHelper.getServerName(this.SERVER));
                    pref.store("MAC", mac);
                }

                if (wolserver != null) {
                    SharedPreferencesHelper pref = new SharedPreferencesHelper(activity, this.USERNAME + '@' + FormatHelper.getServerName(this.SERVER));
                    pref.store("WOLSERVER", wolserver);
                }

                if (host != null) {
                    SharedPreferencesHelper pref = new SharedPreferencesHelper(activity, this.USERNAME + '@' + FormatHelper.getServerName(this.SERVER));
                    pref.store("HOST", host);
                }

                if (wsport != null && !wsport.equals("")) {
                    SharedPreferencesHelper pref = new SharedPreferencesHelper(activity, this.USERNAME + '@' + FormatHelper.getServerName(this.SERVER));
                    pref.store("WSPORT", wsport);
                }

                if (mainPlugins != null) {
                    SharedPreferencesHelper pref = new SharedPreferencesHelper(activity, this.USERNAME + '@' + FormatHelper.getServerName(this.SERVER));
                    pref.store("MAINPLUGINS", mainPlugins);
                }

                if (hashMap.containsKey("seamless") && hashMap.get("seamless") instanceof HashMap) {
                    HashMap<Object, Object> seamless = (HashMap<Object, Object>) hashMap.get("seamless");

                    if (seamless.containsKey("name") && seamless.get("name") instanceof String) {
                        String name = (String) seamless.get("name");

                        String page = "home";
                        if (seamless.containsKey("page") && seamless.get("page") instanceof String) {
                            page = (String) seamless.get("page");
                        }

                        String command = "";
                        if (seamless.containsKey("command") && seamless.get("command") instanceof String) {
                            command = (String) seamless.get("command");
                        }

                        if (activity instanceof MainActivity && this.seamlessActivated) {
                            ((MainActivity) activity).isOpeningPlugin = true;
                            ((MainActivity) activity).openPlugin(name, page, command);
                        }
                    }
                }

                SharedPreferencesHelper pref = new SharedPreferencesHelper(activity, this.USERNAME + '@' + FormatHelper.getServerName(this.SERVER));
            } else {
                LoginHelper.deleteAccount(this.activity, this.CURRENT_ACCOUNT);

                addUserAccount();
            }
        }
    }

    public static void deleteAccount(Context context, Account account) {
        String accountType = context.getResources().getString(R.string.account_type);
        AccountManager accountManager = AccountManager.get(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.removeAccountExplicitly(account);
        } else {
            accountManager.removeAccount(account, null, null);
        }
    }

    private void addUserAccount() {
        Intent intent = new Intent(activity, AddNewAccountActivity.class);
        activity.startActivityForResult(intent, MainActivity.ADD_USER);
    }

    private void selectUserAccount(Account[] accounts) {
        if (!this.isNetworkAvailable()) {
            Intent i = new Intent(activity, OfflineActivity.class);
            activity.finish();  //Kill the activity from which you will go to next activity
            activity.startActivity(i);

            Log.d("network", "off");
        } else {

            ArrayList<Account> accountList = new ArrayList<>();

            Collections.addAll(accountList, accounts);

            if (accountList.size() == 1 && activity instanceof MainActivity) {
                Intent data = new Intent();
                data.putExtra("account", accountList.get(0));
                ((MainActivity) activity).selectAccount(data);

                if (this.intent != null)
                    ((MainActivity) activity).handleIntent(this.intent, true);
            } else {
                Intent intent = new Intent(activity, LoginSelectActivity.class);
                intent.putParcelableArrayListExtra("accounts", accountList);
                if (this.intent != null)
                    intent.putExtra("intent", this.intent);
                activity.startActivityForResult(intent, MainActivity.SELECT_USER);
            }
        }
    }

    public void selectUserAccount(Account acc) {
        this.SERVER = FormatHelper.getServerUrl(LoginHelper.getServerNameFromAccountName(acc.name));
        this.USERNAME_TMP = LoginHelper.getUsernameFromAccountName(acc.name);

        CURRENT_ACCOUNT = acc;

        Logs.v(this, "Change authtoken");

        accountManager.getAuthToken(acc, "login", null, activity, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    AUTHTOKEN = future.getResult().getString("authtoken");

                    onAuthtokenGet(AUTHTOKEN);

                    loginUser(SERVER + "api/login.php", CURRENT_ACCOUNT, AUTHTOKEN);
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

    public void openSelectUserAccount(Intent pIntent) {
        this.intent = pIntent;
        this.openSelectUserAccount();
    }

    public void openSelectUserAccount() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    OPEN_SELECT_USERS);
            return;
        }

        Account[] accounts = accountManager.getAccountsByType(accountType);

        if (accounts != null && accounts.length > 0 && accounts[0] != null) {
            this.selectUserAccount(accounts);
        } else {
            this.addUserAccount();
        }
    }

    public static String getUsernameFromAccountName(String name) {
        String[] names = name.split("@");

        if(!names[0].isEmpty()) {
            return names[0];
        } else {
            return "";
        }
    }

    public static String getServerNameFromAccountName(String name) {
        String[] names = name.split("@");

        if(!names[1].isEmpty()) {
            return names[1];
        } else {
            return "";
        }
    }

    public String getAuthtoken() {
        return this.AUTHTOKEN;
    }

    public String getUsername() {
        return this.USERNAME;
    }

    public String getServer() {
        return this.SERVER;
    }

    public Account getAccount() {
        return this.CURRENT_ACCOUNT;
    }

    public Account getAccount(String user) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    OPEN_SELECT_USERS);
            return null;
        }

        Account[] accounts = accountManager.getAccountsByType(accountType);

        if (accounts != null && accounts.length > 0 && accounts[0] != null) {
            for (Account account : accounts) {
                if(account.name.equals(user)) {
                    return account;
                }
            }

            return accounts[0];
        }

        return null;
    }

    public String getIdentifier() {
        if(this.USERNAME != null) {
            return LoginHelper.getIdentifier(this.SERVER, this.USERNAME);
        }

        return LoginHelper.getIdentifier(this.SERVER, this.USERNAME_TMP);
    }

    public static String getIdentifier(String server, String username) {
        return username + "@" + FormatHelper.getServerName(server);
    }

    public Activity getActivity() {
        return activity;
    }

    public int getAccountCount() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    OPEN_SELECT_USERS);
            return 0;
        }

        Account[] accounts = accountManager.getAccountsByType(accountType);

        if (accounts != null && accounts.length > 0 && accounts[0] != null) {
            return accounts.length;
        }

        return 0;
    }

    public static Object getAccount(Activity activity, String server, String username) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    OPEN_SELECT_USERS);
            return null;
        }

        String accountName = LoginHelper.getIdentifier(server, username);

        AccountManager accountManager = AccountManager.get(activity);
        Account[] accounts = accountManager.getAccountsByType(activity.getResources().getString(R.string.account_type));
        for (Account account : accounts) {
            if (account.name.equalsIgnoreCase(accountName)) {
                return account;
            }
        }

        return null;
    }

    public void onAuthtokenGet(String authToken) {
        for (OnAuthtokenGetListener hl : authtokenGetListeners)
            hl.onAuthtokenGet(authToken);

        authtokenGetListeners.clear();
    }

    public void setOnAuthtokenGetListener(OnAuthtokenGetListener authtokenGetListener) {
        if(this.AUTHTOKEN != null && !this.AUTHTOKEN.equals("")) {
            authtokenGetListener.onAuthtokenGet(this.AUTHTOKEN);
        } else {
            this.authtokenGetListeners.add(authtokenGetListener);
        }
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public void setTemporaryAuthtoken(String temporaryAuthtoken) {
        this.AUTHTOKEN = temporaryAuthtoken;
    }
}
