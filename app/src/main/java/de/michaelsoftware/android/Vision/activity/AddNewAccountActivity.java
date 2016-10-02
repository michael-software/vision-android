package de.michaelsoftware.android.Vision.activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.account.AccountGeneral;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.LoginHelper;
import de.michaelsoftware.android.Vision.tools.SecurityHelper;
import de.michaelsoftware.android.Vision.tools.network.JsonParserAsync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class is called when the user want to create an account in the configuration of Android.
 */
public class AddNewAccountActivity extends Activity {

    /**
     * The tag utilized for the log.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = AddNewAccountActivity.class.getSimpleName();
    public static final String ARG_ACCOUNT_TYPE = "ARG_ACCOUNT_TYPE";
    public static final String ARG_AUTH_TYPE = "ARG_AUTH_TYPE";
    public static final int OPEN_SELECT_USERS = 1;

    /**
     * The context of the program.
     */
    private Activity context;

    /**
     * The user name input by the user.
     */
    private EditText usernameET;

    /**
     * The password input by the user.
     */
    private EditText passwordET;

    private EditText serverET;

    /**
     * The response passed by the service.
     * It is used to give the user name and the password to the account manager
     */
    private AccountAuthenticatorResponse response;

    /**
     * The account manager used to request and add account.
     */
    private AccountManager accountManager;

    private String key;
    private String iv;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState The state saved previously
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Lock the screen orientation
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.add_new_account_layout);

        context = this;

        usernameET = (EditText) findViewById(R.id.username);

        passwordET = (EditText) findViewById(R.id.password);
        serverET = (EditText) findViewById(R.id.server);
        serverET.setTextColor(Color.GRAY);

        /*
      The button to add a new account.
     */
        Button addNewAccountButton = (Button) findViewById(R.id.createNewAccountButton);
        addNewAccountButton.setOnClickListener(onClickListener);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            /*
             * Pass the new account back to the account manager
             */
            response = extras.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case OPEN_SELECT_USERS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    this.onBackPressed();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onBackPressed() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    OPEN_SELECT_USERS);

            return;
        }

        if (accountManager == null) {
            accountManager = AccountManager.get(this);
        }

        String accountType = this.getResources().getString(R.string.account_type);
        Account[] accounts = accountManager.getAccountsByType(accountType);

        ArrayList<Account> accountList = new ArrayList<>();

        Collections.addAll(accountList, accounts);

        if(accountList.size() != 0) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putParcelableArrayListExtra("accounts", accountList);
            this.startActivity(intent);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * The listener for the button pressed.
     */
    public final View.OnClickListener onClickListener = new View.OnClickListener() {
        private Account newUserAccount;
        private String username;
        private String password;
        private String server;

        @Override
        public void onClick(View v) {

            username = usernameET.getText().toString();
			password = passwordET.getText().toString();
            server   = serverET.getText().toString();

			// Check the contents
			// Check the user name
			if (username == null || username.equalsIgnoreCase("")) {
				Toast.makeText(context, getResources().getString(R.string.warning_username_empty), Toast.LENGTH_LONG).show();
				return;
			}

			// Check the password
			if (password == null || password.equalsIgnoreCase("")) {
				Toast.makeText(context, getResources().getString(R.string.warning_password_empty), Toast.LENGTH_LONG).show();
				return;
			}

            // Check the server
            if (server == null || server.equalsIgnoreCase("")) {
                Toast.makeText(context, getResources().getString(R.string.warning_password_empty), Toast.LENGTH_LONG).show();
                return;
            }

            accountManager = AccountManager.get(context);

            /*
             * Check if the account already exists.
             */
            if (LoginHelper.getAccount(context, server, username) != null) {
                Toast.makeText(context, R.string.warning_account_already_exists, Toast.LENGTH_SHORT).show();
                return;
            }

            /*
             * Check the user name and the password against the server.
             */
            newUserAccount = new Account(username+"@"+FormatHelper.getServerName(server), getResources().getString(R.string.account_type));
            String url = FormatHelper.getServerUrl(server)+"ajax.php?action=login";

            LoginHelper.getUserData(url, username, password, this, "getUserInfo");

            Log.d("Add new Account", newUserAccount.name);
        }

        @SuppressWarnings("unused") // used by invoke from HttpPost
        public void getUserInfo(String pResult, String pKey, String pIv) {
            key = pKey;
            iv = pIv;
            Log.d("USEERINFO", pResult);
            if(pResult != null && !pResult.equals("")) {
                JsonParserAsync async = new JsonParserAsync();
                async.setOutput(this, "getUserInfo");
                async.execute(pResult);
            }
        }

        @SuppressWarnings("unused") // used by invoke from JsonParser
        public void getUserInfo(HashMap<Object, Object> hashMap) {
            Log.d("USEERINFO", hashMap.toString());
            if(LoginHelper.isLoggedIn(hashMap, key, iv)) {
                Bundle userInfo = new Bundle();
                userInfo.putString("server", server);

                if(hashMap.containsKey("authtoken") && hashMap.get("authtoken") instanceof String) {
                    String authtoken = SecurityHelper.decrypt((String) hashMap.get("authtoken"), key, iv);

                    String pass = SecurityHelper.encrypt(password);

                    boolean accountCreated = accountManager.addAccountExplicitly(newUserAccount, pass, userInfo);

                    Log.d("RESONSE", response+"");
                    accountManager.setAuthToken(newUserAccount, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, authtoken);
                    if (accountCreated) {
                        if (response != null) {
                            Bundle result = new Bundle();
                            result.putString(AccountManager.KEY_ACCOUNT_NAME, username+"@"+FormatHelper.getServerName(server));
                            result.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                            result.putString(AccountManager.KEY_ACCOUNT_TYPE, getString(R.string.account_type));
                            response.onResult(result);
                            Toast.makeText(context, R.string.add_new_account_done, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(context, R.string.error_creating_account, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, R.string.error_login_account, Toast.LENGTH_LONG).show();
                }

                finish();
            }
        }
    };
}
