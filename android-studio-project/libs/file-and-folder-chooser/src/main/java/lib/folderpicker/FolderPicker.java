package lib.folderpicker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;

public class FolderPicker extends Activity {

    public static final String EXTRA_DATA = "data";

    protected static final String EXTRA_THEME             = "theme";
    protected static final String EXTRA_TITLE             = "title";
    protected static final String EXTRA_DESCRIPTION       = "desc";
    protected static final String EXTRA_LOCATION          = "location";
    protected static final String EXTRA_HOME_BUTTON       = "homeButton";
    protected static final String EXTRA_EMPTY_FOLDER      = "emptyFolder";
    protected static final String EXTRA_NEW_FILE_PROMPT   = "newFilePrompt";
    protected static final String EXTRA_NEW_FILE_NAME     = "newFileName";
    protected static final String EXTRA_PICK_FILE         = "pickFile";
    protected static final String EXTRA_PICK_FILE_PATTERN = "pickFilePattern";

    protected static final Comparator<FilePojo> comparatorAscending = new Comparator<FilePojo>() {
        @Override
        public int compare(FilePojo f1, FilePojo f2) {
            return f1.getName().compareTo(f2.getName());
        }
    };

    private static final int PERMISSIONS_REQUEST_CODE = 0;

    protected final ArrayList<FilePojo> mFolderAndFileList = new ArrayList<FilePojo>();

    protected Intent     mReceivedIntent;
    protected TextView   mTvLocation;
    protected String     mHomeLocation;
    protected boolean    mEmptyFolder;
    protected String     mNewFilePrompt;
    protected String     mNewFileName;
    protected boolean    mPickFile;
    protected Pattern    mFilePattern;
    protected FileFilter mFileFilter;

    protected String     mLocation;
    protected View       mListView;

    private   boolean    mExitOnNextBackPressed;

    public static FolderPickerBuilder withBuilder() {
        return new FolderPickerBuilder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isExternalStorageReadable()) {
            Toast.makeText(FolderPicker.this, getString(R.string.no_access_to_storage), Toast.LENGTH_LONG).show();
            finish();
        }

        mReceivedIntent = getIntent();
        process_intent_EXTRA_THEME();
        initContentView();
        initListView();
        mTvLocation = (TextView) findViewById(R.id.fp_tv_location);

        process_intent_EXTRA_TITLE();
        process_intent_EXTRA_DESCRIPTION();
        process_intent_EXTRA_LOCATION();
        process_intent_EXTRA_HOME_BUTTON();
        process_intent_EXTRA_EMPTY_FOLDER();
        process_intent_EXTRA_NEW_FILE_PROMPT();
        process_intent_EXTRA_NEW_FILE_NAME();
        process_intent_EXTRA_PICK_FILE();
        process_intent_EXTRA_PICK_FILE_PATTERN();

        if (mHomeLocation == null)
            mHomeLocation = Environment.getExternalStorageDirectory().getAbsolutePath();

        checkPermissionsAndLoadLists();
    }

    protected void initContentView() {
        setContentView(R.layout.fp_main_layout);
    }

    private void process_intent_EXTRA_THEME() {
        int default_theme = -999;
        int theme = default_theme;
        try {
            if (mReceivedIntent.hasExtra(EXTRA_THEME)) {
                theme = mReceivedIntent.getIntExtra(EXTRA_THEME, default_theme);
            }
            if (theme != default_theme) {
                handle_intent_EXTRA_THEME(theme);
            }
            else {
                handle_no_intent_EXTRA_THEME();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handle_intent_EXTRA_THEME(int theme) {
        setTheme(theme);
    }

    protected void handle_no_intent_EXTRA_THEME() {
    }

    private void process_intent_EXTRA_TITLE() {
        try {
            if (mReceivedIntent.hasExtra(EXTRA_TITLE)) {
                String title = mReceivedIntent.getStringExtra(EXTRA_TITLE);
                if (title != null) {
                    handle_intent_EXTRA_TITLE(title);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handle_intent_EXTRA_TITLE(String title) {
        ((TextView) findViewById(R.id.fp_tv_title)).setText(title);
    }

    private void process_intent_EXTRA_DESCRIPTION() {
        try {
            if (mReceivedIntent.hasExtra(EXTRA_DESCRIPTION)) {
                String desc = mReceivedIntent.getStringExtra(EXTRA_DESCRIPTION);
                if (desc != null) {
                    handle_intent_EXTRA_DESCRIPTION(desc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handle_intent_EXTRA_DESCRIPTION(String desc) {
        TextView textView = (TextView) findViewById(R.id.fp_tv_desc);
        textView.setVisibility(View.VISIBLE);
        textView.setText(desc);
    }

    private void process_intent_EXTRA_LOCATION() {
        try {
            if (mReceivedIntent.hasExtra(EXTRA_LOCATION)) {
                String newLocation = mReceivedIntent.getStringExtra(EXTRA_LOCATION);
                if (newLocation != null) {
                    handle_intent_EXTRA_LOCATION(newLocation);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handle_intent_EXTRA_LOCATION(String newLocation) {
        File folder = new File(newLocation);
        if (folder.exists())
            mHomeLocation = newLocation;
    }

    private void process_intent_EXTRA_HOME_BUTTON() {
        try {
            if (mReceivedIntent.hasExtra(EXTRA_HOME_BUTTON)) {
                boolean homeButton = mReceivedIntent.getBooleanExtra(EXTRA_HOME_BUTTON, false);
                handle_intent_EXTRA_HOME_BUTTON(homeButton);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handle_intent_EXTRA_HOME_BUTTON(boolean homeButton) {
        if (homeButton) {
            ((TextView) findViewById(R.id.fp_btn_home)).setVisibility(View.VISIBLE);
        }
    }

    private void process_intent_EXTRA_EMPTY_FOLDER() {
        try {
            if (mReceivedIntent.hasExtra(EXTRA_EMPTY_FOLDER)) {
                mEmptyFolder = mReceivedIntent.getBooleanExtra(EXTRA_EMPTY_FOLDER, false);
                handle_intent_EXTRA_EMPTY_FOLDER();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handle_intent_EXTRA_EMPTY_FOLDER() {
        if (mEmptyFolder) {
            ((TextView) findViewById(R.id.fp_tv_empty_dir)).setVisibility(View.VISIBLE);
        }
    }

    private void process_intent_EXTRA_NEW_FILE_PROMPT() {
        try {
            if (mReceivedIntent.hasExtra(EXTRA_NEW_FILE_PROMPT)) {
                String prompt = mReceivedIntent.getStringExtra(EXTRA_NEW_FILE_PROMPT);
                if (prompt != null) {
                    handle_intent_EXTRA_NEW_FILE_PROMPT(prompt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handle_intent_EXTRA_NEW_FILE_PROMPT(String prompt) {
        mNewFilePrompt = prompt;
    }

    private void process_intent_EXTRA_NEW_FILE_NAME() {
        try {
            if (mReceivedIntent.hasExtra(EXTRA_NEW_FILE_NAME)) {
                String name = mReceivedIntent.getStringExtra(EXTRA_NEW_FILE_NAME);
                if (name != null) {
                    handle_intent_EXTRA_NEW_FILE_NAME(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handle_intent_EXTRA_NEW_FILE_NAME(String name) {
        mNewFileName = name;
    }

    private void process_intent_EXTRA_PICK_FILE() {
        try {
            if (mReceivedIntent.hasExtra(EXTRA_PICK_FILE)) {
                mPickFile = mReceivedIntent.getBooleanExtra(EXTRA_PICK_FILE, false);
                handle_intent_EXTRA_PICK_FILE();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handle_intent_EXTRA_PICK_FILE() {
        if (mPickFile) {
            ((TextView) findViewById(R.id.fp_btn_select)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.fp_btn_new)).setVisibility(View.GONE);
        }
    }

    private void process_intent_EXTRA_PICK_FILE_PATTERN() {
        try {
            if (mReceivedIntent.hasExtra(EXTRA_PICK_FILE_PATTERN)) {
                String pickFilePattern = mReceivedIntent.getStringExtra(EXTRA_PICK_FILE_PATTERN);
                if (pickFilePattern != null) {
                    handle_intent_EXTRA_PICK_FILE_PATTERN(pickFilePattern);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handle_intent_EXTRA_PICK_FILE_PATTERN(String pickFilePattern) {
        mFilePattern = Pattern.compile(pickFilePattern, Pattern.CASE_INSENSITIVE);

        mFileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;
                if (!file.isFile()) return false;

                String filename = file.getName();
                return mFilePattern.matcher(filename).matches();
            }
        };
    }

    /**
     * Checks if external storage is available to at least read
     */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Cleans up directory paths entered manually
     *   ex: trailing directory separator, "/./", "/../", etc
     */
    private String normalizeLocation(String location) {
        if (location == null) return null;

        location = location.trim();
        if (location.isEmpty()) return null;

        try {
            File folder = new File(location);
            return folder.getCanonicalPath();
        }
        catch(Exception e) {
            return null;
        }
    }

    private boolean checkAndLoadLists(String location, boolean showToast) {
        if (checkLocation(location, showToast)) {
            mLocation = location;
            mExitOnNextBackPressed = false;
            loadLists();
            return true;
        }
        return false;
    }

    private boolean checkAndLoadLists(String location) {
        return checkAndLoadLists(location, true);
    }

    /**
     * Check location and load lists if location is correct.
     * @param location
     * @param showToast
     * @return boolean
     */
    private boolean checkLocation(String location, boolean showToast) {
        if ((location == null) || location.isEmpty()) {
            if (showToast) {
                Toast.makeText(FolderPicker.this, R.string.dir_is_not_exist, Toast.LENGTH_LONG).show();
            }
            return false;
        }

        File folder = new File(location);

        if (!folder.exists()) {
            if (showToast) {
                Toast.makeText(FolderPicker.this, R.string.dir_is_not_exist, Toast.LENGTH_LONG).show();
            }
            return false;
        }

        if (!folder.isDirectory()) {
            if (showToast) {
                Toast.makeText(FolderPicker.this, R.string.is_not_dir, Toast.LENGTH_LONG).show();
            }
            return false;
        }

        if (!folder.canRead()) {
            if (showToast) {
                Toast.makeText(FolderPicker.this, R.string.dir_read_permission_denied, Toast.LENGTH_LONG).show();
            }
            return false;
        }

        return true;
    }

    /**
     * Load lists and show.
     */
    private void loadLists() {
        try {
            File folder = new File(mLocation);

            mTvLocation.setText(String.format(getString(R.string.location_mask), folder.getAbsolutePath()));
            File[] files = folder.listFiles(mFileFilter);

            ArrayList<FilePojo> mFoldersList = new ArrayList<FilePojo>();
            ArrayList<FilePojo> mFilesList   = new ArrayList<FilePojo>();

            for (File currentFile : files) {
                if (currentFile.isDirectory()) {
                    FilePojo filePojo = new FilePojo(currentFile.getName(), true);
                    mFoldersList.add(filePojo);
                } else if (currentFile.isFile()) {
                    FilePojo filePojo = new FilePojo(currentFile.getName(), false);
                    mFilesList.add(filePojo);
                }
            }

            // sort & add to final List - as we show folders first add folders first to the final list
            Collections.sort(mFoldersList, comparatorAscending);
            mFolderAndFileList.clear();
            mFolderAndFileList.addAll(mFoldersList);

            //if we have to show files, then add files also to the final list
            if (mPickFile) {
                Collections.sort(mFilesList, comparatorAscending );
                mFolderAndFileList.addAll(mFilesList);
            }

            showList();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void initListView() {
        try {
            FolderAdapter folderAdapter = new FolderAdapter(FolderPicker.this, mFolderAndFileList);
            ListView listView = (ListView) findViewById(R.id.fp_listView);
            listView.setAdapter(folderAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    listClick(position);
                }
            });

            mListView = (View) listView;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showList() {
        ListView listView = (ListView) mListView;
        FolderAdapter folderAdapter = (FolderAdapter) listView.getAdapter();
        folderAdapter.notifyDataSetChanged();
    }

    /**
     * Click on the list item.
     * @param position
     */
    protected void listClick(int position) {
        if (mPickFile && !mFolderAndFileList.get(position).isFolder()) {
            String data = mLocation + (mLocation.equals(File.separator) ? "" : File.separator) + mFolderAndFileList.get(position).getName();
            exit(data);
        } else {
            String location = mLocation + (mLocation.equals(File.separator) ? "" : File.separator) + mFolderAndFileList.get(position).getName();
            checkAndLoadLists(location);
        }
    }

    /**
     * Create new folder.
     * @param filename
     */
    protected void createNewFolder(String filename) {
        try {
            File file = new File(mLocation + (mLocation.equals(File.separator) ? "" : File.separator) + filename);
            file.mkdirs();
            checkAndLoadLists(mLocation);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(FolderPicker.this, String.format(getString(R.string.error_string_mask), e.toString()), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show dialog to enter name for new folder or file
     */
    private void newFolderOrFileDialog(boolean isFolder) {
        File folder = new File(mLocation);
        boolean showToast = true;

        if (!folder.canWrite()) {
            if (showToast) {
                Toast.makeText(FolderPicker.this, R.string.dir_write_permission_denied, Toast.LENGTH_LONG).show();
            }
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(FolderPicker.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(FolderPicker.this);
        View view = inflater.inflate(R.layout.dialog_folder_name, null);
        builder.setView(view);
        builder.setTitle(
            isFolder
              ? getString(R.string.enter_folder_name)
              : mNewFilePrompt
        );

        final EditText et = (EditText) view.findViewById(R.id.edit_text);

        if (!isFolder && (mNewFileName != null))
            et.setText(mNewFileName, TextView.BufferType.EDITABLE);

        final AlertDialog dialog = builder.create();
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.create),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String name = et.getText().toString().trim();
                        if (name.isEmpty()) return;

                        if (isFolder) {
                            createNewFolder(name);
                        }
                        else {
                            if ((mFilePattern != null) && !mFilePattern.matcher(name).matches()) {
                                Toast.makeText(FolderPicker.this, getString(R.string.new_file_name_does_not_pass_filter), Toast.LENGTH_LONG).show();
                                return;
                            }

                            String data = mLocation + (mLocation.equals(File.separator) ? "" : File.separator) + name;

                            File file = new File(data);
                            if (file.exists()) {
                                Toast.makeText(FolderPicker.this, getString(R.string.new_file_name_already_exists), Toast.LENGTH_LONG).show();
                                return;
                            }

                            exit(data);
                        }
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (DialogInterface.OnClickListener)null);

        dialog.show();
    }

    /**
     * Edit path manually.
     * @param v
     *
     * note: called from xml layout
     */
    public void edit(View v) {
        LayoutInflater inflater = LayoutInflater.from(FolderPicker.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(FolderPicker.this);
        View view = inflater.inflate(R.layout.dialog_edit_location, null);
        builder.setView(view);
        builder.setTitle(getString(R.string.edit_location));

        final EditText et = (EditText) view.findViewById(R.id.edit_text);
        if (mLocation != null) {
            et.setText(mLocation);
            et.setSelection(mLocation.length());
        }

        final AlertDialog dialog = builder.create();
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String location = et.getText().toString();
                        location = normalizeLocation(location);
                        checkAndLoadLists(location);
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (DialogInterface.OnClickListener)null);

        dialog.show();
    }

    /**
     * Change current directory to default path;
     * @param v
     *
     * note: called from xml layout
     */
    public void home(View v) {
        checkAndLoadLists(mHomeLocation);
    }

    /**
     * Load upper level path or exit.
     * @param v
     *
     * note: called from xml layout
     */
    public void goBack(View v) {
        boolean is_target_up_button = (v != null);

        if ((mLocation == null) || mLocation.isEmpty() || (mLocation.equals("/") && mExitOnNextBackPressed)) {
            exit();
            return;
        }

        if (mLocation.equals("/")) {
            cannotGoBack(is_target_up_button);
            return;
        }

        int start          = mLocation.lastIndexOf('/');
        String newLocation = (start == 0) ? File.separator : mLocation.substring(0, start);
        boolean showToast  = !mExitOnNextBackPressed;
        boolean OK         = checkAndLoadLists(newLocation, showToast);

        if (OK) {
          //mExitOnNextBackPressed = false;
          return;
        }

        if (mExitOnNextBackPressed) {
            exit();
            return;
        }

        cannotGoBack(is_target_up_button);
    }

    private void cannotGoBack(boolean is_target_up_button) {
        int resId = is_target_up_button ? R.string.press_up_button_again_to_exit : R.string.press_back_button_again_to_exit;
        Toast.makeText(FolderPicker.this, getString(resId), Toast.LENGTH_LONG).show();
        mExitOnNextBackPressed = true;
    }

    @Override
    public void onBackPressed(){
        goBack(null);
    }

    /**
     * Show dialog to enter new folder name;
     * @param v
     *
     * note: called from xml layout
     */
    public void newFolderDialog(View v) {
        newFolderOrFileDialog(true);
    }

    /**
     * Select the destination folder or file.
     * @param v
     *
     * note: called from xml layout
     */
    public void select(View v) {
        if (mPickFile) {
            Toast.makeText(FolderPicker.this, getString(R.string.select_file), Toast.LENGTH_LONG).show();
        } else if (mNewFilePrompt != null) {
            newFolderOrFileDialog(false);
        } else if (mReceivedIntent != null) {
            if (mEmptyFolder && !isDirEmpty(mLocation)) {
                Toast.makeText(FolderPicker.this, getString(R.string.select_empty_folder), Toast.LENGTH_LONG).show();
                return;
            }
            mReceivedIntent.putExtra(EXTRA_DATA, mLocation);
            setResult(RESULT_OK, mReceivedIntent);
            finish();
        }
    }

    /**
     * Check if folder is empty.
     * @param path
     * @return boolean
     */
    private boolean isDirEmpty(String path) {
        File dir = new File(path);
        File[] childs = dir.listFiles();
        return (childs == null || childs.length == 0);
    }

    /**
     * Set result (RESULT_CANCELED) and finish activity.
     * @param v
     *
     * note: called from xml layout
     */
    public void cancel(View v) {
        exit();
    }

    /**
     * Set result and finish activity.
     */
    private void exit() {
        exit(null);
    }

    private void exit(String data) {
        if ((data == null) || data.isEmpty()) {
            setResult(RESULT_CANCELED, mReceivedIntent);
        }
        else {
            mReceivedIntent.putExtra(EXTRA_DATA, data);
            setResult(RESULT_OK, mReceivedIntent);
        }
        finish();
    }

    // =========================================================================
    // Check Runtime Permissions
    // =========================================================================

    private void checkPermissionsAndLoadLists() {
        if (Build.VERSION.SDK_INT < 23) {
            checkAndLoadLists(mHomeLocation);
        } else if (Build.VERSION.SDK_INT < 30) {
            String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{permission}, PERMISSIONS_REQUEST_CODE);
            } else {
                checkAndLoadLists(mHomeLocation);
            }
        } else {
            if (Environment.isExternalStorageManager()) {
                checkAndLoadLists(mHomeLocation);
            } else {
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(permissionIntent, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkAndLoadLists(mHomeLocation);
                } else {
                    // permission denied: cancel
                    exit();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (Environment.isExternalStorageManager()) {
                    checkAndLoadLists(mHomeLocation);
                } else {
                    // permission denied: cancel
                    exit();
                }
            }
        }
    }
}
