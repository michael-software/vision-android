package de.michaelsoftware.android.Vision.tools.gui.listener;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.activity.MainActivity;

/**
 * Created by Michael on 22.01.2016.
 * Used for managing the close of the search bar
 */
public class CustomOnActionExpandListener implements MenuItemCompat.OnActionExpandListener {
    private MainActivity mainActivity;
    private boolean openHome = false;

    public CustomOnActionExpandListener(MainActivity pMainActivity) {
        this.mainActivity = pMainActivity;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        if(item.getItemId() == R.id.action_search) {
            mainActivity.closeDrawer();

            mainActivity.getSearchView().setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                private String lastSearch = "";

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText != null && !newText.trim().equals("") && !newText.trim().equals(lastSearch)) {
                        mainActivity.manageSearchInput(newText.trim());
                        lastSearch = newText.trim();
                        return false;
                    } else {
                        mainActivity.manageSearchInput("");
                    }

                    return false;
                }
            });
        }

        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        if(item.getItemId() == R.id.action_search) {
            if(this.openHome) {
                mainActivity.openPluginNoHistory(mainActivity.getCurrentName(), mainActivity.getCurrentView(), mainActivity.getCurrentParameter());
                this.openHome = false;
            }

            mainActivity.getSearchView().setOnQueryTextListener(null);
        }

        return true;
    }

    public void setOpenHome(boolean pBool) {
        this.openHome = pBool;
    }
}
