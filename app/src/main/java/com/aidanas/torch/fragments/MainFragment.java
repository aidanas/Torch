package com.aidanas.torch.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aidanas.torch.Const;
import com.aidanas.torch.R;
import com.aidanas.torch.interfaces.CommonFrag;

/**
 * @author Aidanas Tamasauskas
 *
 * Fragment to hold the LED ON/OFF logic
 */
public class MainFragment extends CommonFrag {

    // Tag.
    public static final String TAG = MainFragment.class.getSimpleName();

    // Light ON/OFF flag
    private boolean isLightOn = false;
    private int oldOrientation;

    // Above flags bundle access identifier.
    private static final String IS_LIGHT_ON = "Is light on?";
    private static final String OLD_ORIENTATION = "Old screen orientation";

    // Holds reference to device's camera.
    private Camera cam;

    // Views
    private View root;
    private Button btn;

    // The fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    // Dialogs
    private Dialog dlgNoFlash;

    // The fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    private OnMainFragmentInteractionListener mListener;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(boolean param1) {

        if (Const.DEBUG) Log.v(MainFragment.class.getName(), "In newInstance()");

        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);

        if (Const.DEBUG) Log.v(TAG, "In onAttach()");

        oldOrientation = getActivity().getRequestedOrientation();

        try {
            mListener = (OnMainFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnMainFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        TODO: for debug purposes, remove when in prod.
//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        if (Const.DEBUG) Log.v(TAG, "In onCreate(), BEFORE state restoration" +
                "\nBundle = " + savedInstanceState +
                "\noldOrientation = " + oldOrientation +
                "\nisLightOn = " + isLightOn);

        // Get arguments of this fragment.
        isLightOn = getArguments().getBoolean(ARG_PARAM1);

        // Restore state is there is one
        if (savedInstanceState != null) {

            if (Const.DEBUG) Log.v(TAG, "savedInstanceState != null, restoring state...");

            isLightOn = savedInstanceState.getBoolean(IS_LIGHT_ON);
            oldOrientation = savedInstanceState.getInt(OLD_ORIENTATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (Const.DEBUG) Log.v(TAG, "In onCreateView(), isLightOn = " + isLightOn);

        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_main, container, false);

        /*
         * If the device has camera flash attach listener to the button.
         */
        if (hasCameraFlash()) {

            btn = (Button) root.findViewById(R.id.ma_btn);

            if (isLightOn)
                btn.setText(R.string.ma_btn_txt_lights_down);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Const.DEBUG) Log.v(TAG, "In onClick(), isLightOn = " + isLightOn);

                    // Toggle the flash.
                    if (isLightOn) {    // Turns the light ON!

                        lightOn(!isLightOn);

                        // Unlock the orientation
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

                        btn.setText(R.string.ma_btn_txt_lights_up);
                        isLightOn = false;

                    } else {    //Turns the light OFF!

                        // Save current orientation of the screen and lock to it.
//                        oldOrientation = getActivity().getRequestedOrientation();
//                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

                        // Get current orientation and lock to it.
//                        if (oldOrientation == Configuration.ORIENTATION_LANDSCAPE) {
//                            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
//                        }
//                        else {
//                            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
//                        }
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);


                        lightOn(!isLightOn);

                        btn.setText(R.string.ma_btn_txt_lights_down);

                        isLightOn = true;

                    }
                }
            });
        } else {
            noFlashAndBye();
        }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Const.DEBUG) Log.v(TAG, "In onStart(), isLightOn = " + isLightOn);

        // Attach to the camera in advance.
        if (this.cam == null)
            this.cam = mListener.getCameraFromActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Const.DEBUG) Log.v(TAG, "In onResume(), isLightOn = " + isLightOn);

        lightOn(isLightOn);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (Const.DEBUG) Log.v(TAG, "In onPause(), isLightOn = " + isLightOn);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (Const.DEBUG) Log.v(TAG, "In onSaveInstanceState()");

        outState.putBoolean(IS_LIGHT_ON, isLightOn);
        outState.putInt(OLD_ORIENTATION, oldOrientation);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Const.DEBUG) Log.v(TAG, "In onStop(), isLightOn = " + isLightOn);
        lightOn(false);
        this.cam = null;

        // Unlock orientation.
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Const.DEBUG) Log.v(TAG, "In onDestroy(), isLightOn = " + isLightOn);

        if (dlgNoFlash != null) {
            dlgNoFlash.cancel();
            dlgNoFlash = null;
        }
    }
    

    @Override
    public void onDetach() {
        super.onDetach();
        if (Const.DEBUG) Log.v(TAG, "In onDetach(), isLightOn = " + isLightOn);

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
    public interface OnMainFragmentInteractionListener {

        void onMainFragmentInteraction(Uri uri);

        Camera getCameraFromActivity();
    }

}
