package com.github.warren_bank.mock_location.ui;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.BookmarkItem;
import com.github.warren_bank.mock_location.data_model.LocPoint;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;
import com.github.warren_bank.mock_location.service.LocationService;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends ActivityGroup {
    private TabHost tabHost;

    // ---------------------------------------------------------------------------------------------
    // Lifecycle Events:
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(getLocalActivityManager());

        String tab_1_tag = getString(R.string.MainActivity_tab_1_tag);
        String tab_2_tag = getString(R.string.MainActivity_tab_2_tag);

        String tab_1_label = getString(R.string.MainActivity_tab_1_label);
        String tab_2_label = getString(R.string.MainActivity_tab_2_label);

        tabHost.addTab(tabHost.newTabSpec(tab_1_tag).setIndicator(tab_1_label).setContent(new Intent(this, FixedPositionActivity.class)));
        tabHost.addTab(tabHost.newTabSpec(tab_2_tag).setIndicator(tab_2_label).setContent(new Intent(this, TripSimulationActivity.class)));

        final Intent intent = getIntent();
        if (intent == null)
            return;

        String currentTabTag = getCurrentTabTag(intent);
        if (currentTabTag != null) {
            tabHost.setCurrentTabByTag(currentTabTag);
        }

        showToast(intent);
    }

    private String getCurrentTabTag(Intent intent) {
        return intent.getStringExtra(getString(R.string.MainActivity_extra_current_tab_tag));
    }

    private void showToast(Intent intent) {
        String text = intent.getStringExtra(getString(R.string.MainActivity_extra_toast));
        if (text == null)
            return;

        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        boolean is_started = LocationService.isStarted();

        if (is_started && !isFinishing())
            finish();
    }

    // ---------------------------------------------------------------------------------------------
    // ActionBar:
    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().setDisplayShowHomeEnabled(false);
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.menu_start_preferences: {
                startPreferences();
                return true;
            }
            case R.id.menu_start_bookmarks: {
                startBookmarks();
                return true;
            }
            case R.id.menu_save_bookmarkitem: {
                saveBookmarkItem();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(menuItem);
            }
        }
    }

    private void startPreferences() {
        Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
        startActivity(intent);
    }

    private void startBookmarks() {
        Intent intent = new Intent(MainActivity.this, BookmarksActivity.class);
        startActivity(intent);
    }

    private void startSaveBookmark(LocPoint point) {
        String title = getExistingBookmark(point);
        if (title != null) {
            Toast.makeText(MainActivity.this, getString(R.string.error_bookmarkitem_exists, title), Toast.LENGTH_SHORT).show();
            return;
        }

        double lat = point.getLatitude();
        double lon = point.getLongitude();

        Intent intent = new Intent(MainActivity.this, BookmarksActivity.class);
        intent.putExtra(getString(R.string.BookmarksActivity_extra_add_lat), lat);
        intent.putExtra(getString(R.string.BookmarksActivity_extra_add_lon), lon);
        startActivity(intent);
    }

    private String getExistingBookmark(LocPoint point) {
        ArrayList<BookmarkItem> arrayList = SharedPrefs.getBookmarkItems(MainActivity.this);
        int index = BookmarkItem.indexOf(arrayList, point.getLatitude(), point.getLongitude());

        return (index == -1) ? null : arrayList.get(index).title;
    }

    private void saveBookmarkItem() {
        String tab_1_tag = getString(R.string.MainActivity_tab_1_tag);
        String tab_2_tag = getString(R.string.MainActivity_tab_2_tag);

        String current_tag = tabHost.getCurrentTabTag();
        View   current_tab = tabHost.getCurrentView();

        TextView origin_view      = null;
        TextView destination_view = null;
        LocPoint origin           = null;
        LocPoint destination      = null;

        if (current_tag.equals(tab_2_tag)) {
            // TripSimulationActivity
            origin_view      = (TextView) current_tab.findViewById(R.id.input_trip_origin);
            destination_view = (TextView) current_tab.findViewById(R.id.input_trip_destination);
        }
        else {
            // FixedPositionActivity
            origin_view = (TextView) current_tab.findViewById(R.id.input_fixed_position);
        }

        if (origin_view != null) {
            origin = getPointFromTextView(origin_view);
        }
        if (destination_view != null) {
            destination = getPointFromTextView(destination_view);
        }

        if ((origin == null) && (destination == null)) {
            Toast.makeText(MainActivity.this, getString(R.string.error_missing_required_value), Toast.LENGTH_SHORT).show();
            return;
        }

        if ((origin != null) && (destination != null)) {
            saveBookmarkItemTab2(origin, destination);
            return;
        }

        if (origin != null) {
            startSaveBookmark(origin);
            return;
        }

        if (destination != null) {
            startSaveBookmark(destination);
            return;
        }
    }

    private LocPoint getPointFromTextView(TextView view) {
        LocPoint point = null;
        String text    = view.getText().toString();
        try {
            point = new LocPoint(text);
        }
        catch(NumberFormatException e) {}
        return point;
    }

    private void saveBookmarkItemTab2(LocPoint origin, LocPoint destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.save_bookmark_menu_title);
        builder.setItems(R.array.save_bookmark_menu_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which + 1) {
                    case 1:
                        // origin
                        dialog.dismiss();
                        startSaveBookmark(origin);
                        break;
                    case 2:
                        // destination
                        dialog.dismiss();
                        startSaveBookmark(destination);
                        break;
                    default:
                        dialog.cancel();
                        break;
                }
            }
        });
        builder.show();
    }
}
