package com.aidanas.torch;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.aidanas.torch.adapters.NavDrawLsAdapter;
import com.aidanas.torch.fragments.MainFragment;
import com.aidanas.torch.fragments.StrobeFragment;

import java.util.Arrays;
import java.util.List;

/**
 * Main activity class. This activity shall allow a user to turn the flash of a the camera on ir off.
 */
public class MainActivity extends AppCompatActivity
        implements MainFragment.OnMainFragmentInteractionListener,
        StrobeFragment.OnStrobeFragmentInteractionListener{

    // Tag for debug.
    private final String TAG = this.getClass().getSimpleName();

    // This might be used in 'auto on' feature in the setting menu is to be implemented.
//    private final boolean paramsToMainFragAutoOn = false;

    private List<String> mDrawerTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private Camera mCam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Const.DEBUG) Log.v(TAG, "In onCreate()");

        setContentView(R.layout.activity_main_drawnav);

        mDrawerTitles = Arrays.asList(getResources().getStringArray(R.array.nav_drawer_titles));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList   = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.navdraw_list_item_layout, mDrawerTitles));

        mDrawerList.setAdapter(new NavDrawLsAdapter(this, mDrawerTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean autoOnPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_AUTO_ON, false);

        FragmentManager fManager = getFragmentManager();

        // Find fragment or create a new one.
        Fragment mainFragment = fManager.findFragmentByTag(MainFragment.TAG);

        if (mainFragment == null){
            mainFragment = MainFragment.newInstance(false);
        }

        // Load main fragment into the main activity frame
        getFragmentManager().beginTransaction().replace(R.id.ma_navdraw_content_frame, mainFragment,
                MainFragment.TAG).commit();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Const.DEBUG) Log.v(TAG, "In onStart()");

        this.mCam = getCamera();
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

        if (Const.DEBUG) Log.v(TAG, "In onSaveInstanceState()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (Const.DEBUG) Log.v(TAG, "In onDestroy()");

    }

    /****************************************************
     * Only Android live cycle methods above this point!
     ****************************************************/

    /**
     * Method configures instantiates and displays a dialog displaying the information
     * for the 'About' screen (Menu item).
     */
    private void showAboutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about_dialog_title);

        // Single button ('OK') dialog.
        builder.setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.setView(this.getLayoutInflater().inflate(R.layout.dialog_about, null));

        builder.show();

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        // For logging purposes.
        private final String TAG = this.getClass().getSimpleName();

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            if (Const.DEBUG) Log.v(TAG, "In onItemClick(), position = " + position);

            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

        Fragment frag;

        // Create a fragment depending on users' selection.
        switch (position){
            case 0:
                frag = MainFragment.newInstance(false);
                break;
            case 1:
                frag = StrobeFragment.newInstance(false);
                break;
            default:
                Log.e(TAG, "Default case of switch statement. Should have never happened.");
                frag = MainFragment.newInstance(false);
                break;
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.ma_navdraw_content_frame, frag)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerTitles.get(position));
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        ActionBar ab = getActionBar();
        if (ab != null){
            getActionBar().setTitle(title);
        }
    }

    /**
     * Interface implementation method to do with MainFragment communication.
     * @param uri
     */
    @Override
    public void onMainFragmentInteraction(Uri uri) {
        return;
    }

    @Override
    public void onStrobeFragmentInteraction(Uri uri) {
        return;
    }

    @Override
    public android.hardware.Camera getCameraFromActivity(){
        return this.mCam;
    }

    /**
     * Method to get and configure camera. Should improve improve user experience due to quicker
     * response time to "Lights ON" request.
     *
     * @return main camera of a device.
     */
    private Camera getCamera(){
        if (Const.DEBUG) Log.v(TAG, "In getCamera()");

        // Open, start and return a camera object.
        Camera cam = Camera.open();
        cam.startPreview();
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
}
