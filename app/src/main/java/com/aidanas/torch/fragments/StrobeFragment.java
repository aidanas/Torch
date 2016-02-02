package com.aidanas.torch.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
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

import com.aidanas.torch.Const;
import com.aidanas.torch.R;
import com.aidanas.torch.interfaces.CommonFrag;

/**
 * @author Aidanas Tamasauskas
 *
 * Fragment to hold the LED ON/OFF logic
 */
public class StrobeFragment extends CommonFrag {

    // Tag.
    public static final String TAG = StrobeFragment.class.getSimpleName();


    // Above flags bundle access identifier.
    private static final String IS_LIGHT_ON = "Is light on?";
    private static final String OLD_ORIENTATION = "Old screen orientation";

    private static final String SAVED_ST_KEY_STROBE_RATE  = "strobe rate";
    private static final String SAVED_ST_KEY_FLASH_LENGTH = "flash length";

    // Holds reference to device's camera.
    private Camera cam;

    /*
     * These two variable will be read by UI thread and written to by background "strobe" thread.
     * "volatile" makes writes and reads atomic. Note: for our purposes there is no need for the
     * "Synchronised" block.
     */
    volatile private long strobeRate = 1040L;   // Initial value
    volatile private long flashLegth = 1030L;   // Initial value

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
        SeekBar.OnSeekBarChangeListener seekBarOListener;
        strobeSb = (SeekBar) root.findViewById(R.id.strobe_frag_strobe_seekbar);
        flashSb = (SeekBar) root.findViewById(R.id.strobe_frag_flash_duration_seekbar);

        strobeSb.setOnSeekBarChangeListener(seekBarOListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (Const.DEBUG) Log.v(TAG, "In onProgressChanged(), seekbar = " + seekBar +
                        ", progress = " + progress);

                if (seekBar == strobeSb) {
                    strobeRate = (100 - progress) * 10 + 40;
                } else if (seekBar == flashSb){
                    flashLegth = (100 - progress) * 10 + 30;
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
        if (this.cam == null)
            this.cam = mListener.getCameraFromActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Const.DEBUG) Log.v(TAG, "In onResume(), Thread = " + Thread.currentThread().getName());

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

        this.cam = null;
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

     /****************************************************
      * Only Android live cycle methods above this point!
      ****************************************************/

    /**
     * MEthod to check the availability of camera flash.
     *
     * @return hasCameraFlash, true if the device has camera flash.
     */
    private boolean hasCameraFlash() {

        boolean hasCameraFlash = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (Const.DEBUG) Log.v(TAG, "In hasCameraFlash(), camera flash" +
                ((hasCameraFlash) ? "" : " NOT") + " detected!");

        return hasCameraFlash;
    }

    /**
     * Method to toggle light on/off
     * @param should true to turn on or false to turn off.
     */
    private void lightOn(boolean should) {

        if (Const.DEBUG) Log.v(TAG, "In lightOn(), should = " + should);

        /*
         * Toggle camera's flash.
         */
        if (should){
            try {
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
            } catch (Exception e) {
                Log.e(getString(R.string.app_name), "failed to open Camera");
                e.printStackTrace();
            }
        } else {
            Camera.Parameters p = this.cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            this.cam.setParameters(p);
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

    public String getTAG() { return TAG; }

    /***********************************************************************************************
     *                                  INTERFACES
     **********************************************************************************************/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnStrobeFragmentInteractionListener {

        void onStrobeFragmentInteraction(Uri uri);

        Camera getCameraFromActivity();
    }

}
