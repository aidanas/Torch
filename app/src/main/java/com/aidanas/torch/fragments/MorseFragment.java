package com.aidanas.torch.fragments;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.aidanas.torch.interfaces.CameraProvider;
import com.aidanas.torch.Const;
import com.aidanas.torch.R;
import com.aidanas.torch.interfaces.CommonFrag;
import com.aidanas.torch.morsetools.MoLetter;
import com.aidanas.torch.morsetools.MoTranslator;
import com.aidanas.torch.morsetools.TransmissionThread;
import com.aidanas.torch.morsetools.Transmitter;

import java.util.List;

/**
 * @author Aidanas Tamasauskas
 * Created on: 03-02-2016
 *
 * This fragment will be used to host the Morse code transmission UI.
 */
public class MorseFragment extends CommonFrag {

    // Tag.
    public static final String TAG = MorseFragment.class.getSimpleName();

    //TODO: this is debug string.
    private static final String TEST_TXT = "ABCDE";

    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnMorseFragInteractionListener mListener;

    // Device's camera whose flash will be used for signaling.
    private Camera mCam;

    // Views.
    private View mRoot;
    private EditText mUserText;
    private TextView mTextTransmitting;

    // Thread which will do the signaling.
    private Thread mTransThread;

    public MorseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MorseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MorseFragment newInstance(String param1) {
        MorseFragment fragment = new MorseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (Const.DEBUG) Log.v(TAG, "In onAttach() (Activity)");

        try {
            mListener = (OnMorseFragInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnMorseFragInteractionListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (Const.DEBUG) Log.v(TAG, "In onAttach() (Context)");

        if (context instanceof OnMorseFragInteractionListener) {
            mListener = (OnMorseFragInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMorseFragInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Const.DEBUG) Log.v(TAG, "In onCreate()");

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

        Log.v(TAG, "Translating text " + TEST_TXT + "\n" + "translated: " +
                MoTranslator.translateToMorse(TEST_TXT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (Const.DEBUG) Log.v(TAG, "In onCreateView()");

        // Inflate the layout for this fragment and get view references.
        mRoot = inflater.inflate(R.layout.fragment_morse, container, false);
        mUserText = (EditText) mRoot.findViewById(R.id.morse_frag_txt_to_transmit_tw);
        mTextTransmitting = (TextView) mRoot.findViewById(R.id.morse_frag_transmitting_tw);

        // Button click listener will initiate the transmission.
        mRoot.findViewById(R.id.morse_frag_transmit_btn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Const.DEBUG) Log.v(TAG, "In onClick()");

                        // If we are transmitting, then cease doing so.
                        if (mTransThread != null) {
                            mTransThread.interrupt();
                            mTransThread = null;
                        }

                        // Get the text from the user.
                        String txt = mUserText.getText().toString();
                        if (Const.DEBUG) Log.v(TAG, "txt to be translated: " + txt);

                        // Translate the text to Morse code.
                        List<MoLetter> txtInMorse = MoTranslator.translateToMorse(txt);

                        // Background thread executing the transmission.
                        mTransThread = new TransmissionThread(getActivity(), mCam, txtInMorse);
                        mTransThread.start();
                    }
                });

        return mRoot;
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
    public void onStop() {
        super.onStop();
        if (Const.DEBUG) Log.v(TAG, "In onAttach()");

        // Stop the transmitting background thread if such exists.
        if (mTransThread != null) mTransThread.interrupt();
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

    public String getTAG() { return TAG; }

    /***********************************************************************************************
     *                                  INTERFACES
     **********************************************************************************************/

    /**
     * This interface ensures that the fragment will be able to obtain a Camera object to use.
     */
    public interface OnMorseFragInteractionListener extends CameraProvider{
        void onMorseFragmentInteraction(Uri uri);
    }

}
