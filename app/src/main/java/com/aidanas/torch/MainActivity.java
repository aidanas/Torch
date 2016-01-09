package com.aidanas.torch;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.aidanas.torch.fragments.MainFragment;

/**
 * Main activity class. This activity shall allow a user to turn the flash of a the camera on ir off.
 */
public class MainActivity extends AppCompatActivity implements MainFragment.OnMainFragmentInteractionListener{

    // Tag for debug.
    private final String TAG = this.getClass().getSimpleName();

    // This might be used in 'auto on' feature in the setting menu is to be implemented.
    private final boolean paramsToMainFragAutoOn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Const.DEBUG) Log.v(TAG, "In onCreate()");

        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean autoOnPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_AUTO_ON, false);

        FragmentManager fManager = getFragmentManager();

        // Find fragment or create a new one.
        Fragment mainFragment = fManager.findFragmentByTag(MainFragment.TAG);

        if (mainFragment == null){
            mainFragment = MainFragment.newInstance(autoOnPref);
        }

        // Load main fragment into the main activity frame
        getFragmentManager().beginTransaction().replace(R.id.ma_frame, mainFragment,
                MainFragment.TAG).commit();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Const.DEBUG) Log.v(TAG, "In onStart()");

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

    /**
     * Interface implementation method to deam with MainFragment communication.
     * @param uri
     */
    @Override
    public void onMainFragmentInteraction(Uri uri) {
        return;
    }
}
