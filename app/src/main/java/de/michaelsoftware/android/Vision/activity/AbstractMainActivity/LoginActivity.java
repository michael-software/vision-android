package de.michaelsoftware.android.Vision.activity.AbstractMainActivity;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;

import de.michaelsoftware.android.Vision.OfflineActivity;
import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.LoginHelper;
import de.michaelsoftware.android.Vision.tools.ThemeUtils;

/**
 * Created by Michael on 12.05.2016.
 */
public abstract class LoginActivity extends HandleIntentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();

        loginHelper = new LoginHelper(this);
        if(intent.hasExtra("action") || (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null)) {
            loginHelper.seamlessActivated = false;
        }

        super.onCreate(savedInstanceState);
    }

    protected void openSelectAccount(Intent intent) {
        if(loginHelper.getUsername() == null || loginHelper.getUsername().equals("")) {
            if (intent.hasExtra("account")) {
                Account account = intent.getParcelableExtra("account");

                //loginHelper = new LoginHelper(this); /* TODO */
                loginHelper.selectUserAccount(account);
            } else if (intent.hasExtra("user")) {
                String user   = intent.getStringExtra("user");

                //loginHelper = new LoginHelper(this);
                Account account = loginHelper.getAccount(user);

                if(account != null) {
                    loginHelper.selectUserAccount(account);
                } else {
                    loginHelper.openSelectUserAccount();
                }
            } else {
                loginHelper.openSelectUserAccount();
            }
        }
    }

    @Override
    protected void onResume() {
        if(mMenu != null && mMenu.findItem(R.id.action_change_account) != null)
            if(loginHelper != null && loginHelper.getAccountCount() == 1) {
                mMenu.findItem(R.id.action_change_account).setVisible(false);
            } else {
                mMenu.findItem(R.id.action_change_account).setVisible(true);
            }

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if(loginHelper != null && loginHelper.getAccountCount() == 1) {
            menu.findItem(R.id.action_change_account).setVisible(false);
        }

        return true;
    }

    public void selectAccount(Intent data) {
        Parcelable result = data.getParcelableExtra("account");

        if (result instanceof Account) {
            /*if (loginHelper != null) {
                loginHelper = null;
            }

            loginHelper = new LoginHelper(this);*/ /* TODO */
            loginHelper.selectUserAccount((Account) result);

            int userTheme = ThemeUtils.getTheme(this, loginHelper.getIdentifier());
            if(ThemeUtils.getCurrentTheme() != userTheme) {
                ThemeUtils.changeToTheme(this, userTheme, loginHelper);
            }

            this.loadMenu();

            if(this.gui != null && this.isOpeningPlugin) {
                this.openHome();
            } else {
                this.isOpeningPlugin = false;
            }
        }
    }
}
