package com.aidanas.torch;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Main activity class. This activity shall allow a user to turn the flash of a the camera on ir off.
 */
public class MainActivity extends AppCompatActivity {

    // Tag for debug.
    private static final String TAG = MainActivity.class.getName();

    // Light ON/OFF flag
    private boolean isLightOn = false;
    private int oldOrientation = getRequestedOrientation();

    // Above flags bundle access identifier.
    private static final String IS_LIGHT_ON = "Is light on?";
    private static final String OLD_ORIENTATION = "Old screen orientation";

    // Holds reference to device's camera.
    private Camera cam;

    // Views
    private Button btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Const.DEBUG) Log.v(TAG, "In onCreate(), BEFORE state restoration" +
                "\nBundle = " + savedInstanceState +
                "\noldOrientation = " + oldOrientation +
                "\nisLightOn = " + isLightOn);

        // Restore state is there is one
        if (savedInstanceState != null) {
            if (Const.DEBUG) Log.v(TAG, "savedInstanceState != null, restoring state...");
            isLightOn = savedInstanceState.getBoolean(IS_LIGHT_ON);
            oldOrientation = savedInstanceState.getInt(OLD_ORIENTATION);
        }

        setContentView(R.layout.activity_main);

        /*
        If the device has camera flash attach listener to the button.
         */
        if (hasCameraFlash()) {

            btn = (Button) findViewById(R.id.ma_btn);

            if (isLightOn)
                btn.setText(R.string.ma_btn_txt_lights_down);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Const.DEBUG) Log.v(TAG, "In onClick(), isLightOn = " + isLightOn);

                    // Toggle the flash.
                    if (isLightOn) {

                        lightOn(!isLightOn);

                        // Restore orientation.
                        setRequestedOrientation(oldOrientation);

                        btn.setText(R.string.ma_btn_txt_lights_up);
                        isLightOn = false;

                    } else {

                        // Save current orientation of the screen and lock to it.
                        oldOrientation = getRequestedOrientation();
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

                        lightOn(!isLightOn);

                        btn.setText(R.string.ma_btn_txt_lights_down);

                        isLightOn = true;

                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Const.DEBUG) Log.v(TAG, "In onStart(), isLightOn = " + isLightOn);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Const.DEBUG) Log.v(TAG, "In onResume(), isLightOn = " + isLightOn);

        lightOn(isLightOn);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Const.DEBUG) Log.v(TAG, "In onPause(), isLightOn = " + isLightOn);

        lightOn(false);

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (Const.DEBUG) Log.v(TAG, "In onPause(), isLightOn = " + isLightOn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_LIGHT_ON, isLightOn);
        outState.putInt(OLD_ORIENTATION, oldOrientation);
    }

    @Override
    protected void onDestroy() {

        if (Const.DEBUG) Log.v(TAG, "In onDestroy()");

        super.onDestroy();
    }

    /*
     * Only Android live cycle methods above this point!
     */

    /**
     * MEthod to check the availability of camera flash.
     *
     * @return hasCameraFlash, true if the device has camera flash.
     */
    private boolean hasCameraFlash() {

        boolean hasCameraFlash = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (Const.DEBUG) Log.v(TAG, "In hasCameraFlash(), camera flash" +
                ((hasCameraFlash) ? "" : " NOT") + " detected!");

        return hasCameraFlash;
    }

    /**
     * Method to toggle light on/off
     * @param should ture to turn on or false to turn off.
     */
    private void lightOn(boolean should) {

        if (Const.DEBUG) Log.v(TAG, "In lightOn(), should = " + should);

        if (should){
            cam = Camera.open();
            Camera.Parameters p = cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);

        } else {
            if (cam != null) {
                cam.release();
            }

        }
    }
}
