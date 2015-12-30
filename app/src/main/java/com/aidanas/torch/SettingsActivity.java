package com.aidanas.torch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.aidanas.torch.fragments.SettingsFragment;

/**
 * Created by Aidanas on 30/12/2015.
 */
public class SettingsActivity extends Activity {

    // Used by an activity to determine what pref to read.
    public static String KEY_PREF_AUTO_ON = "pref_auto_on";

    // Tag for debug.
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Const.DEBUG) Log.v(TAG, "In onCreate()");

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, SettingsFragment.newInstance("param1"))
                .commit();
    }
}
