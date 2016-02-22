package com.aidanas.torch.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aidanas.torch.interfaces.CurrentIndexReceiver;
import com.aidanas.torch.interfaces.CameraProvider;
import com.aidanas.torch.Const;
import com.aidanas.torch.R;
import com.aidanas.torch.interfaces.CommonFrag;
import com.aidanas.torch.morsetools.MoLetter;
import com.aidanas.torch.morsetools.MoTranslator;
import com.aidanas.torch.morsetools.TransmissionThread;

import java.util.List;

/**
 * @author Aidanas Tamasauskas
 * Created on: 03-02-2016
 *
 * This fragment will be used to host the Morse code transmission UI.
 */
public class MorseFragment extends CommonFrag implements CurrentIndexReceiver {

    // Tag.
    public static final String TAG = MorseFragment.class.getSimpleName();

    //TODO: this is debug string.
    private static final String TEST_TXT = "ABCDE";

    private static final String ARG_PARAM1 = "param1";

    // Keys for save Bundle access.
    private static final String IS_TRANSMITTING = "is transmitting?";
    private static final String CURRENT_INDEX   = "current letter";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnMorseFragInteractionListener mListener;

    // Device's camera whose flash will be used for signaling.
    private Camera mCam;

    // Views.
    private View mRoot;
    private EditText mUserText;
    private TextView mTextTransmitting;
    private SeekBar mSeekBar;
    private Button mTransmitBtn;

    // Thread which will do the signaling.
    private TransmissionThread mTransThread;

    // Parameters to transmitter.
    private boolean mIsTransmitting = false;
    private int mCurrenIndex = 0;
    private String mTextToTransmit;


    /**
     * No param empty constructor.
     */
    public MorseFragment() {}

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

        // Get args, as set by the factory.
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

        // Get previous state if exists.
        if (savedInstanceState != null){
            mIsTransmitting = savedInstanceState.getBoolean(IS_TRANSMITTING);
            mCurrenIndex    = savedInstanceState.getInt(CURRENT_INDEX);
            Log.v(TAG, "Sate RESTORED:\nmIsTransmitting = " + mIsTransmitting +
                    ", mCurrenIndex = " + mCurrenIndex);
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
        mSeekBar = (SeekBar) mRoot.findViewById(R.id.morse_frag_transmitsion_rate_sb);
        mUserText = (EditText) mRoot.findViewById(R.id.morse_frag_txt_to_transmit_tw);
        mTransmitBtn =  (Button) mRoot.findViewById(R.id.morse_frag_transmit_btn);
        mTextTransmitting = (TextView) mRoot.findViewById(R.id.morse_frag_transmitting_tw);

        if (mIsTransmitting) mTransmitBtn.setText(R.string.STOP);

        /*
         * Button click listener will initiate the transmission.
         */
        mTransmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Const.DEBUG) Log.v(TAG, "In onClick()");

                /*
                 * If currently transmitting then stop. Otherwise validate input and start
                 * transmission.
                 */
                if (mIsTransmitting && mTransThread != null) {
                    stopTransmission();
                    mTransmitBtn.setText(R.string.transmit);
                    mIsTransmitting = false;
                    // Is the input text valid?
                } else if (mUserText.getText().toString().matches(MoTranslator.VALID_REGEX)) {
                    startTransmission(0);
                    mTransmitBtn.setText(R.string.STOP);
                } else {
                    Toast.makeText(MorseFragment.this.getActivity(), "Invalid Text!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*
         * Setup the listener for transmission rate seek bar.
         */
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (Const.DEBUG) Log.v("OnSeekBarChangeListener", "progress = " + progress);

                if (mTransThread != null && fromUser) mTransThread.updateSignalUnit(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            } // Not used

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }  // Not used
        });

        return mRoot;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Const.DEBUG) Log.v(TAG, "In onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Const.DEBUG) Log.v(TAG, "In onResume() mIsTransmitting = " + mIsTransmitting +
                ", mCurrenIndex = " + mCurrenIndex);

        if (mCam == null) mCam = mListener.getDeviceCamera();

        // If transmission was interrupted, continue where we left off.
        if (mIsTransmitting){
            startTransmission(mCurrenIndex);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (Const.DEBUG) Log.v(TAG, "In onSaveInstanceState(), mIsTransmitting = " +
                mIsTransmitting);

        // Save  readings if transmission if happening.
        if (mIsTransmitting){
            outState.putBoolean(IS_TRANSMITTING, mTransThread.isTransmitting());
            outState.putInt(CURRENT_INDEX, mTransThread.getCurrentIndex());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Const.DEBUG) Log.v(TAG, "In onPause()");

        mCam = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Const.DEBUG) Log.v(TAG, "In onStop()");

        // Stop the transmitting background thread if such exists and save stuff for later.
        if (mTransThread != null) {
            mIsTransmitting = mTransThread.isTransmitting();
            mCurrenIndex = mTransThread.getCurrentIndex();
            mTransThread.interrupt();
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (Const.DEBUG) Log.v(TAG, "In onDetach()");
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Const.DEBUG) Log.v(TAG, "In onDestroy()");
    }

    /***********************************************************************************************
     *                            Only Android live cycle methods above this point!
     **********************************************************************************************/

    public String getTAG() { return TAG; }

    /**
     * Method to setup the transmission thread and initiate the transmission of the text.
     *
     * @param offset - Position index in the text indicating starting position.
     */
    private void startTransmission(int offset) {

        stopTransmission();

        // Get the text from the user.
        String txt = mUserText.getText().toString();
        if (Const.DEBUG) Log.v(TAG, "txt to be translated: " + txt);

        // Translate the text to Morse code.
        List<MoLetter> txtInMorse = MoTranslator.translateToMorse(txt);

        // Display text to be transmitted in the status view.
        mTextToTransmit = txt;
        mTextTransmitting.setText(txt);

        // Background thread executing the transmission.
        mTransThread = new TransmissionThread(mCam, txtInMorse, mTextTransmitting, this);

        if (offset != 0) {
            mTransThread.setCurrentIndex(offset);
        }

        mTransThread.updateSignalUnit(mSeekBar.getProgress());
        mTransThread.start();
        mIsTransmitting = true;
    }

    /**
     * Method to terminate current transmission.
     */
    private void stopTransmission() {
        // If we are transmitting, then cease doing so.
        if (mTransThread != null) {
            mTransThread.interrupt();
            mTransThread = null;
        }

        // Delete the currently transmitting text.
        mTextTransmitting.setText("");
    }

    /***********************************************************************************************
     *                                Interface Implementations
     **********************************************************************************************/

    /**
     * Receives uypdates about the currently being transmitted character index in the text.
     * @param index - Intex in the text.
     */
    @Override
    public void newIndex(int index) {

        if (index == -1){
            mTextTransmitting.setText(mTextToTransmit);
        } else {
            SpannableString ss = new SpannableString(mTextToTransmit);

            // Set the siZe of the teXt...
            ss.setSpan(new RelativeSizeSpan(2f), index, index + 1, 0);

            // ...and the colour.
            ss.setSpan(new ForegroundColorSpan(getActivity().getResources()
                    .getColor(R.color.colour_sch1_colour1)), index, index + 1, 0);
            ss.setSpan(new BackgroundColorSpan(getActivity().getResources()
                    .getColor(R.color.colour_sch1_colour4)), index, index + 1, 0);
            mTextTransmitting.setText(ss);
        }
    }

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
