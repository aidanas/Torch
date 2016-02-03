package com.aidanas.torch.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.aidanas.torch.Const;
import com.aidanas.torch.R;


/**
 * Settings fragment. //TODO: implement when needed.
 *
 * @author Aidanas Tamasauskas
 * @date 2015
 */
public class SettingsFragment extends PreferenceFragment {

    // Tag for debug.
    private final String TAG = this.getClass().getSimpleName();


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnSettingsFragmentListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1) {

        if (Const.DEBUG) Log.v(SettingsFragment.class.getName(), "In newInstance()");

        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (Const.DEBUG) Log.v(TAG, "In onAttach()");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Const.DEBUG) Log.v(TAG, "In onCreate()");

        // Get arguments of the fragment.
        mParam1 = getArguments().getString(ARG_PARAM1);

        // Restore state is there is one.
        if (savedInstanceState != null) {
            if (Const.DEBUG) Log.v(TAG, "savedInstanceState != null, restoring state...");

        }

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Const.DEBUG) Log.v(TAG, "In onStart()");

    }

    @Override
    public void onResume() {
        super.onResume();

        if (Const.DEBUG) Log.v(TAG, "In onResume()");

    }

    @Override
    public void onPause() {
        super.onPause();

        if (Const.DEBUG) Log.v(TAG, "In onPause()");

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (Const.DEBUG) Log.v(TAG, "In onSaveInstanceState()");
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Const.DEBUG) Log.v(TAG, "In onPause()");
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**********************************************************************************************
     * Only Android live cycle methods above this point!
     **********************************************************************************************/

    /**
     * /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSettingsFragmentListener {
        // TODO: Update argument type and name
        void onSettingsFragmentInteraction(Uri uri);
    }

}
