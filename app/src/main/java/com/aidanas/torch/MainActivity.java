package com.aidanas.torch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.aidanas.torch.adapters.NavDrawLsAdapter;
import com.aidanas.torch.fragments.MainFragment;
import com.aidanas.torch.fragments.MorseFragment;
import com.aidanas.torch.fragments.StrobeFragment;
import com.aidanas.torch.interfaces.CommonFrag;

import java.util.Arrays;
import java.util.List;

/**
 * Main activity class. This activity shall allow a user to turn the flash of a the camera on ir off.
 */
public class MainActivity extends AppCompatActivity implements
        MainFragment.OnMainFragmentInteractionListener,
        StrobeFragment.OnStrobeFragmentInteractionListener,
        MorseFragment.OnMorseFragInteractionListener{

    private static final String NAV_DRAW_SELECTED_POS = "selected item";

    // Tag for debug.
    private final String TAG = this.getClass().getSimpleName();

    // Views os the activity.
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private LinearLayout mDrawerContent;

    // Navigation drawer item titles.
    private List<String> mDrawerTitles;

    // Action bar default and navdraw open titles.
    private CharSequence mDrawerTitle = "Menu";
    private CharSequence mTitle = "Torch";

    // Selected item in the navigation drawer.
    private int mSelectedItem = AdapterView.INVALID_POSITION; // By default no items selected.

    // Camera whose flash is going to be used.
    private Camera mCam;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Const.DEBUG) Log.v(TAG, "In onCreate(), savedInstanceState = " + savedInstanceState);

        setContentView(R.layout.activity_main_drawnav);

        // Deals with navigation drawer creation and setup including action bar config.
        createNavDrawer();

        FragmentManager fManager = getSupportFragmentManager();

        // Find fragment or create a new one.
        Fragment frag = fManager.findFragmentByTag(MainFragment.TAG);

        if (savedInstanceState != null){
            selectItem(savedInstanceState.getInt(NAV_DRAW_SELECTED_POS));
        }
        else {
            frag = MainFragment.newInstance(false);
            // Load main fragment into the main activity frame
            getSupportFragmentManager().beginTransaction().replace(R.id.ma_navdraw_content_frame,
                    frag, MainFragment.TAG).commit();
        }

        // Prevent the device from going to sleep.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (Const.DEBUG) Log.v(TAG, "In onPostCreate");

        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Const.DEBUG) Log.v(TAG, "In onStart()");

        mCam = getCamera();

        // No point to continue if camera unavailable.
        if (mCam == null) {
            noCameraExitWithDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Const.DEBUG) Log.v(TAG, "In onResume()");

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Const.DEBUG) Log.v(TAG, "In onPause()");

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Const.DEBUG) Log.v(TAG, "In onStop()");

        releaseCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Const.DEBUG) Log.v(TAG, "In onCreateOptionsMenu()");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Const.DEBUG) Log.v(TAG, "In onOptionsItemSelected()");

        /*
         * Pass the event to ActionBarDrawerToggle, if it returns true,
         * then it has handled the app icon touch event.
         */
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        switch (id) {
            case R.id.action_about:
                showAboutDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (Const.DEBUG) Log.v(TAG, "In onSaveInstanceState(), mSelectedItem = " + mSelectedItem);

        // Save current selected item in the nav drawer.
        if (mSelectedItem != AdapterView.INVALID_POSITION) {
            outState.putInt(NAV_DRAW_SELECTED_POS, mSelectedItem);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Const.DEBUG) Log.v(TAG, "In onDestroy()");

        // Allow the device to go to sleep.
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * This and onPostCreate() requred in our case for the navigation drawer to open/close upon
     * home/up button clicked.
     * @param newConfig - New configuration.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /***********************************************************************************************
     * Only Android live cycle methods above this point!
     **********************************************************************************************/

    /**
     * Method to take care of the navigation bar setup logic performed in onCreate() method.
     * Also takes care of the home button on the action bar and its functionality.
     */
    private void createNavDrawer() {
        if (Const.DEBUG) Log.v(TAG, "In createNavDrawer()");

        // Get nav draw views.
        mDrawerLayout  = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList    = (ListView) findViewById(R.id.left_drawer);
        mDrawerContent = (LinearLayout) findViewById(R.id.nav_draw_content_ll);

        mDrawerTitles = Arrays.asList(getResources().
                getStringArray(R.array.nav_drawer_titles));

        // Set the adapter for the list view.
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.navdraw_list_item_layout, mDrawerTitles));

        mDrawerList.setAdapter(new NavDrawLsAdapter(this, mDrawerTitles));

        // Set the list's click listener.
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open,
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                // Save current title for restoration after the drawer is closed.
                ActionBar ab = getSupportActionBar();
                if (ab != null) mTitle = ab.getTitle();

                setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Enable home button to be used for nav drawer open/close function.
        ActionBar ab = getSupportActionBar();
        if (ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
        }
    }

    /**
     * Method configures instantiates and displays a dialog displaying the information
     * for the 'About' screen (Menu item).
     */
    private void showAboutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about_dialog_title);

        // Rate this App button, opens Playstore to rate this app.
        builder.setPositiveButton(R.string.rate_app, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" +
                        MainActivity.this.getPackageName()));
                startActivity(intent);
            }
        });

        // Cancel, button.
        builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.setView(this.getLayoutInflater().inflate(R.layout.dialog_about, null));

        builder.show();

    }

    /**
     * Method informs the user that camera could not be opened and exits.
     */
    private void noCameraExitWithDialog() {
        if (Const.DEBUG) Log.v(TAG, "In noCameraExitWithDialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exit_dlg_title);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });

        builder.setMessage("Activity could not obtain access to the camera. Some other activity " +
                getString(R.string.exit_dlg_message));
        builder.setCancelable(false);
        builder.show();
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

        if (Const.DEBUG) Log.v(TAG, "In selectItem, position = " + position);

        CommonFrag frag;
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Create a fragment depending on users' selection.
        switch (position){
            case 0:
                frag = (CommonFrag) fragmentManager.findFragmentByTag(MainFragment.TAG);
                if (frag == null ) frag = MainFragment.newInstance(false);
                break;
            case 1:
                frag = (CommonFrag) fragmentManager.findFragmentByTag(StrobeFragment.TAG);
                if (frag == null ) frag = StrobeFragment.newInstance(false);
                break;
            case 2:
                frag = (CommonFrag) fragmentManager.findFragmentByTag(MorseFragment.TAG);
                if (frag == null ) frag = MorseFragment.newInstance(null);
                break;
            default:
                Log.e(TAG, "Default case of switch statement. THis should have never happened. O_o");
                frag = MainFragment.newInstance(false);
                break;
        }

        mSelectedItem = position;

        // Insert the fragment by replacing any existing fragment
        fragmentManager.beginTransaction()
                .replace(R.id.ma_navdraw_content_frame, frag, frag.getTAG())
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        mTitle = mDrawerTitles.get(position);
        setTitle(mTitle);

        mDrawerLayout.closeDrawer(mDrawerContent);
    }

    /**
     * Method to set the ActionBar title.
     * @param title - New title.
     */
    @Override
    public void setTitle(CharSequence title) {
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setTitle(title);
    }

    /**
     * Method to get and configure camera. Should improve improve user experience due to quicker
     * response time to "Lights ON" request.
     *
     * @return - Main camera of a device.
     */
    private Camera getCamera(){
        if (Const.DEBUG) Log.v(TAG, "In getCamera()");

        // Open, start and return a camera object.
        Camera cam = Camera.open();

        if (cam != null){
            cam.startPreview();
        }

        return cam;
    }

    /**
     * Release camera if it is used at the moment.
     */
    private void releaseCamera() {
        if (Const.DEBUG) Log.v(TAG, "In releaseCamera(), this.cam = " + this.mCam);

        if (this.mCam != null) {
            this.mCam.stopPreview();
            this.mCam.release();
            this.mCam = null;
        }
    }

    /***********************************************************************************************
     *                                  INTERFACE IMPLEMENTATIONS
     **********************************************************************************************/

    @Override
    public Camera getDeviceCamera(){
        return this.mCam;
    }

    @Override
    public void releaseCamera(Camera cam){ releaseCamera(); }

    /**
     * Interface implementations for comunication with fragments. Not used at the moment.
     * @param uri
     */
    @Override
    public void onMainFragmentInteraction(Uri uri) { return; }

    @Override
    public void onStrobeFragmentInteraction(Uri uri) { return; }

    @Override
    public void onMorseFragmentInteraction(Uri uri) { return; }

    /***********************************************************************************************
     *                                         Private Classes
     **********************************************************************************************/

    /**
     * Navigation drawer click listener.
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        // For logging purposes.
        private final String TAG = this.getClass().getSimpleName();

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            if (Const.DEBUG) Log.v(TAG, "In onItemClick(), position = " + position);

            selectItem(position);
        }
    }

}
