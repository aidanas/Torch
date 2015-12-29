package com.aidanas.torch.fragments;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aidanas.torch.Const;
import com.aidanas.torch.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMainFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    // Tag for debug.
    private final String TAG = this.getClass().getSimpleName();

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


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnMainFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1) {

        if (Const.DEBUG) Log.v(MainFragment.class.getName(), "In newInstance()");

        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
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

        if (Const.DEBUG) Log.v(TAG, "In onCreate(), BEFORE state restoration" +
                "\nBundle = " + savedInstanceState +
                "\noldOrientation = " + oldOrientation +
                "\nisLightOn = " + isLightOn);

        // Get arguments of this fragment.
        mParam1 = getArguments().getString(ARG_PARAM1);

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
                    if (isLightOn) {

                        lightOn(!isLightOn);

                        // Restore orientation.
                        getActivity().setRequestedOrientation(oldOrientation);

                        btn.setText(R.string.ma_btn_txt_lights_up);
                        isLightOn = false;

                    } else {

                        // Save current orientation of the screen and lock to it.
                        oldOrientation = getActivity().getRequestedOrientation();
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

                        lightOn(!isLightOn);

                        btn.setText(R.string.ma_btn_txt_lights_down);

                        isLightOn = true;

                    }
                }
            });
        }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Const.DEBUG) Log.v(TAG, "In onStart(), isLightOn = " + isLightOn);

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

        lightOn(false);

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

        if (Const.DEBUG) Log.v(TAG, "In onPause(), isLightOn = " + isLightOn);
    }


    @Override
    public void onDetach() {
        super.onDetach();
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
        // TODO: Update argument type and name
        void onMainFragmentInteraction(Uri uri);
    }

}
