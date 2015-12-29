package com.aidanas.torch;

import android.net.Uri;
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

    private final String paramsToMainFrag = "none";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Const.DEBUG) Log.v(TAG, "In onCreate()");

        setContentView(R.layout.activity_main);

        // Load main fragment into the main activity frame
        getFragmentManager().beginTransaction().add(R.id.ma_frame, MainFragment.newInstance(paramsToMainFrag)).commit();

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

        if (Const.DEBUG) Log.v(TAG, "In onPause()");
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

        if (id == R.id.action_settings) {
            return true;
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
     * Interface implementation method to deam with MainFragment communication.
     * @param uri
     */
    @Override
    public void onMainFragmentInteraction(Uri uri) {
        return;
    }
}
