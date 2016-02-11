package com.aidanas.torch.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.aidanas.torch.CameraProvider;
import com.aidanas.torch.Const;
import com.aidanas.torch.R;
import com.aidanas.torch.interfaces.CommonFrag;

/**
 * @author Aidanas Tamasauskas
 * Created on: 03-02-2016
 *
 * This fragment contains the logicto make that binking light.
 * It creates a background thread which periodically turns the LED on and off with varying intervals
 * in between the ON and OFF commands.
 *
 */
public class StrobeFragment extends CommonFrag {

    // Tag.
    public static final String TAG = StrobeFragment.class.getSimpleName();

    private static final String SAVED_ST_KEY_STROBE_RATE  = "strobe rate";
    private static final String SAVED_ST_KEY_FLASH_LENGTH = "flash length";

    // Holds reference to device's camera.
    private Camera mCam;

    /*
     * Light on and off duration in milliseconds. Not guaranteed to be precise as the implementation
     * uses Thread.sleep().
     * These two variable will be read by UI thread and written to by background "strobe" thread.
     * "volatile" makes writes and reads atomic. Note: for our purposes there is no need for the
     * "Synchronised" block.
     */
    volatile private long strobeRate = 1040L;   // Initial value
    volatile private long flashLegth = 1030L;   // Initial value

    /*
     * Constants to adjust the durations of the light ON and OFF periods. Used with following:
     * strobeRate = (SEEKBAR_MAX_VALUE - progress) * SEEKBAR_VAL_MULTIPLIER + STROBE_RATE_VAL_OFFSET
     */
    private static final int SEEKBAR_MAX_VALUE = 100;
    private static final int SEEKBAR_VAL_MULTIPLIER = 10;
    private static final int STROBE_RATE_VAL_OFFSET = 40;
    private static final int FLASH_LENGTH_VAL_OFFSET = 30;

    // Views
    private View root;
    private SeekBar strobeSb;
    private SeekBar flashSb;

    // The fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    // Dialogs
    private Dialog dlgNoFlash;

    // The fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    private OnStrobeFragmentInteractionListener mListener;

    private Thread strobeThread;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StrobeFragment newInstance(boolean param1) {

        if (Const.DEBUG) Log.v(StrobeFragment.class.getName(), "In newInstance()");

        StrobeFragment fragment = new StrobeFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public StrobeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);

        if (Const.DEBUG) Log.v(TAG, "In onAttach()");

        try {
            mListener = (OnStrobeFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnMainFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Const.DEBUG) Log.v(TAG, "In onCreate()");

        // Restore state is there is one.
        if (savedInstanceState != null) {
            if (Const.DEBUG) Log.v(TAG, "savedInstanceState != null, restoring state...");
            strobeRate = savedInstanceState.getLong(SAVED_ST_KEY_STROBE_RATE);
            flashLegth = savedInstanceState.getLong(SAVED_ST_KEY_FLASH_LENGTH);
            if (Const.DEBUG) Log.v(TAG, "restored to strobeRate = " + strobeRate +
                    ", flashLegth = " + flashLegth);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (Const.DEBUG) Log.v(TAG, "In onCreateView()");

        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_strobe, container, false);

        /*
         * Two seek bars and their on change listener.
         */
        flashSb = (SeekBar) root.findViewById(R.id.strobe_frag_light_off_sb);
        strobeSb = (SeekBar) root.findViewById(R.id.strobe_frag_light_on_sb);
        SeekBar.OnSeekBarChangeListener seekBarOListener;
        strobeSb.setOnSeekBarChangeListener(seekBarOListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (Const.DEBUG) Log.v(TAG, "In onProgressChanged(), seekbar = " + seekBar +
                        ", progress = " + progress);

                if (seekBar == strobeSb) {
                    strobeRate = (SEEKBAR_MAX_VALUE - progress) * SEEKBAR_VAL_MULTIPLIER + STROBE_RATE_VAL_OFFSET;
                } else if (seekBar == flashSb) {
                    flashLegth = (SEEKBAR_MAX_VALUE - progress) * SEEKBAR_VAL_MULTIPLIER + FLASH_LENGTH_VAL_OFFSET;
                }

                if (Const.DEBUG) Log.v(TAG, "In onProgressChanged(), Thread = " +
                        Thread.currentThread().getName() + ", strobeRate = " + strobeRate +
                        ", flashLegth = " + flashLegth);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        flashSb.setOnSeekBarChangeListener(seekBarOListener);

        return root;
    }


    @Override
    public void onStart() {
        super.onStart();

        if (Const.DEBUG) Log.v(TAG, "In onStart()");

        // Attach to the camera in advance.
        if (this.mCam == null)
            this.mCam = mListener.getDeviceCamera();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Const.DEBUG) Log.v(TAG, "In onResume(), Thread = " + Thread.currentThread().getName());

        /*
         * Launch a separate thread to toggle the LED ON/OFF, so it would not stall the UI thread.
         */
        strobeThread = new Thread(new Runnable() {
            public void run() {
                if (Const.DEBUG) Log.v(TAG, "In run(), Thread = " + Thread.currentThread().getName());

                try {
                    while (true) {
                        if (Const.DEBUG) Log.v(TAG, "In run(), Thread = " +
                                Thread.currentThread().toString() + ", strobeRate = " + strobeRate +
                                ", flashLegth = " + flashLegth);

                        lightOn(true);
                        Thread.sleep(flashLegth);
                        lightOn(false);
                        Thread.sleep(strobeRate);

                    }
                } catch (InterruptedException e) {
                    // Tsss.. its all going to be over soon.
                }
            }
        });

        strobeThread.start();

    }

    @Override
    public void onPause() {
        super.onPause();

        if (Const.DEBUG) Log.v(TAG, "In onPause()");

        // Kill the strobes thread.
        if (strobeThread != null) strobeThread.interrupt();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (Const.DEBUG) Log.v(TAG, "In onSaveInstanceState(), saving state:\n" +
                "strobeRate = " + strobeRate + ", flashLegth = " + flashLegth);

        outState.putLong(SAVED_ST_KEY_FLASH_LENGTH, flashLegth);
        outState.putLong(SAVED_ST_KEY_STROBE_RATE, strobeRate);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Const.DEBUG) Log.v(TAG, "In onStop()");

        this.mCam = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Const.DEBUG) Log.v(TAG, "In onDestroy()");

        if (dlgNoFlash != null) {
            dlgNoFlash.cancel();
            dlgNoFlash = null;
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (Const.DEBUG) Log.v(TAG, "In onDetach()");

        mListener = null;
    }

    /***********************************************************************************************
     *                            Only Android live cycle methods above this point!
     **********************************************************************************************/

    /**
     * MEthod to check the availability of camera flash.
     *
     * @return hasCameraFlash, true if the device has camera flash.
     */
    private boolean hasCameraFlash() {

        boolean hasCameraFlash = getActivity().getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (Const.DEBUG) Log.v(TAG, "In hasCameraFlash(), camera flash" +
                ((hasCameraFlash) ? "" : " NOT") + " detected!");

        return hasCameraFlash;
    }

    /**
     * Method to toggle light on`/off
     * @param should true to turn on or false to turn off.
     */
    private void lightOn(boolean should) {
        if (Const.DEBUG) Log.v(TAG, "In lightOn(), should = " + should);

        /*
         * As this method is called from the background thread it is possible that the device camera
         * is not yet initiated. Especially after configuration change event.
         */
        if (mCam == null) return;

        /*
         * Null pointer exception may be thrown here since the mCam object is accessed by both
         * background and UI threads. When the strobe rate is high and configuration change occurs
         * mCam might not be yet initialised by UI thread. I know, it is a dirty hack but it does
         * the job until we figure out the proper approach.
         */
        try {
            /*
             * Toggle camera's flash.
             */
            if (should){
                Camera.Parameters p = mCam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCam.setParameters(p);
            } else {
                Camera.Parameters p = mCam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCam.setParameters(p);
            }
        } catch (Exception e) { //TODO: null pointer exceptions should be avoided not cached.
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
    }

    /**
     * Method to inform the user that their device has no required hardware (camera flash) and exit.
     */
    private void noFlashAndBye() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(R.string.no_flash_found);

        // Single button ('OK') dialog.
        builder.setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                getActivity().finish();
            }
        });
        builder.setCancelable(false);
        builder.setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_no_flash_found, null));
        this.dlgNoFlash = builder.create();
        this.dlgNoFlash.show();
    }

    /**
     * Returns a string representing the TAG for this class. Used by fragment manager.
     * @return - Fragments' tag.
     */
    public String getTAG() { return TAG; }

    /***********************************************************************************************
     *                                  INTERFACES
     **********************************************************************************************/

    /**
     * This interface ensures that the fragment will be able to obtain a Camera object to use.
     */
    public interface OnStrobeFragmentInteractionListener extends CameraProvider {
        void onStrobeFragmentInteraction(Uri uri);
    }

}
