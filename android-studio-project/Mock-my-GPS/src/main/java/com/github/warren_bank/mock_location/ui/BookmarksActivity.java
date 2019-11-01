package com.github.warren_bank.mock_location.ui;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.BookmarkItem;
import com.github.warren_bank.mock_location.data_model.LocPoint;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class BookmarksActivity extends Activity {
    private ListView                    listView;
    private ArrayList<BookmarkItem>     listItems;
    private ArrayAdapter<BookmarkItem>  listAdapter;

    // ---------------------------------------------------------------------------------------------
    // Data Mutation:
    // ---------------------------------------------------------------------------------------------

    private void handleDataSetChange() {
        listAdapter.notifyDataSetChanged();
        SharedPrefs.putBookmarkItems(BookmarksActivity.this, listItems);
    }

    // ---------------------------------------------------------------------------------------------
    // Lifecycle Events:
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        listView    = (ListView) findViewById(R.id.listview);
        listItems   = SharedPrefs.getBookmarkItems(BookmarksActivity.this);
        listAdapter = new ArrayAdapter<BookmarkItem>(BookmarksActivity.this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showOpenDialog(position);
                return;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showEditDialog(position);
                return true;
            }
        });

        onNewIntent(getIntent());
    }

	@Override
	protected void onNewIntent(Intent intent) {
        if (intent == null)
            return;

        double lat = intent.getDoubleExtra(getString(R.string.BookmarksActivity_extra_add_lat), 2000.0);
        double lon = intent.getDoubleExtra(getString(R.string.BookmarksActivity_extra_add_lon), 2000.0);
        if ((lat > 1000) && (lon > 1000))
            return;

        showAddDialog(lat, lon);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!isFinishing())
            finish();
    }

    // ---------------------------------------------------------------------------------------------
    // ActionBar:
    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().setDisplayShowHomeEnabled(false);
        getMenuInflater().inflate(R.menu.activity_bookmarks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.menu_add_bookmarkitem: {
                showAddDialog();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(menuItem);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Open Dialog:
    // ---------------------------------------------------------------------------------------------

    private void showOpenDialog(final int position) {
        final BookmarkItem listItem = listItems.get(position);
        final LocPoint point        = listItem.toPoint();

        AlertDialog.Builder builder = new AlertDialog.Builder(BookmarksActivity.this);
        builder.setTitle(R.string.open_bookmark_menu_title);
        builder.setItems(R.array.open_bookmark_menu_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which + 1) {
                    case 1:
                        // fixed position
                        dialog.dismiss();
                        handleFixedPosition(point);
                        break;
                    case 2:
                        // trip origin
                        dialog.dismiss();
                        handleTripOrigin(point);
                        break;
                    case 3:
                        // trip destination
                        dialog.dismiss();
                        handleTripDestination(point);
                        break;
                    default:
                        dialog.cancel();
                        break;
                }
            }
        });
        builder.show();
    }

    private void handleFixedPosition(LocPoint point) {
        SharedPrefs.putTripOrigin(BookmarksActivity.this, point);
        startMainActivity(0, null);
    }

    private void handleTripOrigin(LocPoint point) {
        SharedPrefs.putTripOrigin(BookmarksActivity.this, point);
        startMainActivity(2, null);
    }

    private void handleTripDestination(LocPoint point) {
        SharedPrefs.putTripDestination(BookmarksActivity.this, point);
        startMainActivity(2, null);
    }

    private void startMainActivity(int tab, String toast) {
        Intent intent = new Intent(BookmarksActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (tab > 0) {
            String current_tab_tag = (tab == 1)
                ? getString(R.string.MainActivity_tab_1_tag)
                : getString(R.string.MainActivity_tab_2_tag)
            ;

            intent.putExtra(getString(R.string.MainActivity_extra_current_tab_tag), current_tab_tag);
        }
        if (toast != null) {
            intent.putExtra(getString(R.string.MainActivity_extra_toast), toast);
        }
        startActivity(intent);
    }

    // ---------------------------------------------------------------------------------------------
    // Add/Edit Dialog:
    // ---------------------------------------------------------------------------------------------

    private void showAddDialog() {
        showEditDialog(-1);
    }

    private void showAddDialog(double lat, double lon) {
        showEditDialog(-1, lat, lon);
    }

    private void showEditDialog(final int position) {
        showEditDialog(position, 2000.0, 2000.0);
    }

    private void showEditDialog(final int position, double new_lat, double new_lon) {
        final boolean isAdd         = (position < 0);
        final boolean isAddLocation = isAdd && (new_lat < 1000) && (new_lon < 1000);

        final BookmarkItem listItem = (isAdd)
          ? new BookmarkItem("", new_lat, new_lon)
          : listItems.get(position)
        ;

        final Dialog dialog = new Dialog(BookmarksActivity.this, R.style.app_theme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_bookmarkitem);

        final EditText input_bookmark_title    = (EditText) dialog.findViewById(R.id.input_bookmark_title);
        final EditText input_bookmark_location = (EditText) dialog.findViewById(R.id.input_bookmark_location);

        final Button button_delete = (Button) dialog.findViewById(R.id.button_delete);
        final Button button_save   = (Button) dialog.findViewById(R.id.button_save);

        input_bookmark_title.setText(listItem.title);

        if (!isAdd || isAddLocation)
            input_bookmark_location.setText(listItem.toPoint().toString());

        if (isAdd)
            button_delete.setText(R.string.label_button_cancel);

        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdd) {
                  listItems.remove(position);
                  handleDataSetChange();
                }
                dialog.dismiss();
            }
        });

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String new_title    = input_bookmark_title.getText().toString().trim();
                final String new_location = input_bookmark_location.getText().toString().trim();

                final boolean same_title    = isAdd ? false : new_title.equals(listItem.title);
                final boolean same_location = isAdd ? false : new_location.equals(listItem.toPoint().toString());

                if (new_title.equals("") || new_location.equals("")) {
                    Toast.makeText(BookmarksActivity.this, getString(R.string.error_missing_required_value), Toast.LENGTH_SHORT).show();
                    return;
                }

                LocPoint new_location_point;
                try {
                    new_location_point = new LocPoint(new_location);
                }
                catch(NumberFormatException e) {
                    Toast.makeText(BookmarksActivity.this, getString(R.string.error_location_format, new_location), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (same_title && same_location) {
                    // no change
                    dialog.dismiss();
                    return;
                }

                if (!same_title) {
                    listItem.title = new_title;
                }
                if (!same_location) {
                    listItem.lat = new_location_point.getLatitude();
                    listItem.lon = new_location_point.getLongitude();
                }

                if (isAdd) {
                    if (!listItems.add(listItem)) {
                        Toast.makeText(BookmarksActivity.this, getString(R.string.error_add_bookmarkitem), Toast.LENGTH_SHORT).show();
                        return;
                    }                    
                }

                handleDataSetChange();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
