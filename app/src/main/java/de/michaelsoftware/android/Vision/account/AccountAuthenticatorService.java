package de.michaelsoftware.android.Vision.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import de.michaelsoftware.android.Vision.activity.AddNewAccountActivity;
import de.michaelsoftware.android.Vision.tools.Logs;

/**
 * Authenticator service that returns a subclass of AbstractAccountAuthenticator in onBind().
 */
public class AccountAuthenticatorService extends Service {

    /**
     * The implementation of the class |AccountAuthenticatorImpl|.
     * It is implemented as a singleton
     */
    private static AccountAuthenticatorImpl sAccountAuthenticator = null;

    /**
     * The main constructor.
     */
    public AccountAuthenticatorService() {
        super();
    }

    /**
     * The bind method of the service.
     * @param intent The intent used to invoke the service
     * @return The binder of the class which has implemented |AbstractAccountAuthenticator|
     */
    @Override
    public IBinder onBind(Intent intent) {
        Logs.v(this, "Binding the service");
        IBinder ret = null;
        if (intent.getAction().equals(AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
            ret = getAuthenticator().getIBinder();
        }
        return ret;
    }

    /**
     * The method used to obtain the authenticator. It is implemented as a singleton
     * @return The implementation of the class |AbstractAccountAuthenticator|
     */
    private AccountAuthenticatorImpl getAuthenticator() {
        if (AccountAuthenticatorService.sAccountAuthenticator == null) {
            AccountAuthenticatorService.sAccountAuthenticator = new AccountAuthenticatorImpl(this);
        }

        return AccountAuthenticatorService.sAccountAuthenticator;
    }

    /**
     * The class which implements the class |AbstractAccountAuthenticator|.
     * It is the one which the Android system calls to perform any action related with the account
     */
    private static class AccountAuthenticatorImpl extends AbstractAccountAuthenticator {

        /**
         * The Context used.
         */
        private final Context mContext;

        /**
         * The main constructor of the class.
         * @param context The context used
         */
        public AccountAuthenticatorImpl(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response,
                String accountType,
                String authTokenType,
                String[] requiredFeatures,
                Bundle options) throws NetworkErrorException {
            Logs.d(this, "Adding new account");
            Bundle reply = new Bundle();

            Logs.d(this, "The auth token type is " + authTokenType);
            Intent i = new Intent(mContext, AddNewAccountActivity.class);
            i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            i.putExtra("AuthTokenType", authTokenType);
            reply.putParcelable(AccountManager.KEY_INTENT, i);

            return reply;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse arg0, Account arg1,
                Bundle arg2) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse arg0, String account) {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle arg3) throws NetworkErrorException {

            final AccountManager am = AccountManager.get(mContext);
            String authToken = am.peekAuthToken(account, authTokenType);

            // Lets give another try to authenticate the user
            if (TextUtils.isEmpty(authToken)) {
                final String password = am.getPassword(account);
                if (password != null) {
                    try {
                        authToken = AccountGeneral.sServerAuthenticate.userSignIn(account.name, password, authTokenType);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            Log.v("TEst", "Hallo" + authToken);

            // If we get an authToken - we return it
            if (!TextUtils.isEmpty(authToken)) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                return result;
            }

            // If we get here, then we couldn't access the user's password - so we
            // need to re-prompt them for their credentials. We do that by creating
            // an intent to display our AuthenticatorActivity.
            final Intent intent = new Intent(mContext, AddNewAccountActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            intent.putExtra(AddNewAccountActivity.ARG_ACCOUNT_TYPE, account.type);
            intent.putExtra(AddNewAccountActivity.ARG_AUTH_TYPE, authTokenType);
            final Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }



        @Override
        public String getAuthTokenLabel(String authTokenType) {
            if (AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
                return AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
            else
                return authTokenType + " (Label)";
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse arg0, Account arg1,
                String[] arg2) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse arg0, Account arg1,
                String arg2, Bundle arg3) throws NetworkErrorException {
            return null;
        }
    }
}

