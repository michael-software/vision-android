package de.michaelsoftware.android.Vision.activity;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.Logs;
import de.michaelsoftware.android.Vision.tools.gui.listener.CustomOnItemClickListener;

public class LoginSelectActivity extends Activity {
    private Account[] accounts;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_select);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        if(intent.hasExtra("intent")) {
            Logs.d(this, "intent transmitted");
            this.intent = (Intent) bundle.get("intent");
        }

        ArrayList<Parcelable> accounts = bundle.getParcelableArrayList("accounts");
        setSelect(accounts);
    }

    public void setSelect(ArrayList<Parcelable> accs) {
        accounts = new Account[accs.size()];

        ArrayList<String> mArray = new ArrayList<>();
        ArrayList<String> mActions = new ArrayList<>();

        int i = 0;

        for(Parcelable temp : accs) {
            if(temp instanceof Account) {
                mArray.add(((Account) temp).name);
                mActions.add(""+i);
                accounts[i] = (Account) temp;
                i++;
            }
        }

        ArrayAdapter mAdapter = new ArrayAdapter<>(this, R.layout.list_item_menu, mArray);

        ListView lv = (ListView) findViewById(R.id.selectUser);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new CustomOnItemClickListener(mActions, this));
    }

    public void selectAccount(int in) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("account", accounts[in]);
        returnIntent.putExtra("intent", this.intent);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void createAccount(View v) {
        Intent intent = new Intent(this, AddNewAccountActivity.class);
        this.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
    }
}
