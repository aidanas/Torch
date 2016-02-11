package com.aidanas.torch.morsetools;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import com.aidanas.torch.Const;
import com.aidanas.torch.R;

import java.util.List;

/**
 * Created by Aidanas
 * Created on 11/02/2016.
 *
 * Thread which does the signaling.
 */
public class TransmissionThread extends Thread implements Transmitter.SignalReceiver{

    // Tag.
    public static final String TAG = TransmissionThread.class.getSimpleName();

    // Context in which the thread runs.
    private final Context mContext;

    // Device camera whose flash will be used for signaling.
    private final Camera mCam;

    // Morse code transmitter.
    private final Transmitter mTransmitter;


    /**
     * Conatructor.
     * @param context - Context in which this thread will be run.
     * @param cam - Device camera object which flash will be used for signaling Morse code.
     */
    public TransmissionThread(Context context, Camera cam, List<MoLetter> txtInMorse){
        this.mContext = context;
        this.mCam = cam;
        mTransmitter = new Transmitter(this, txtInMorse);
    }

    /**
     * Entry point of the thread.
     */
    @Override
    public void run(){
        try {
            mTransmitter.startTransmission();
        } catch (InterruptedException e) {
            if (Const.DEBUG) Log.v(TAG, "Thread = " + Thread.currentThread().getName() +
                    "interrupted");
            e.printStackTrace();
        }
    }

    /**
     * Callback method for SignalReceiver interface. Receives signal from transmitter and delegates
     * it to lightOn method.
     * @param signalType - Signal on / Signal off.
     */
    @Override
    public void signal(boolean signalType) {
        if (Const.DEBUG) Log.v(TAG, "In signal() received = " + signalType + ", Thread = " +
                        Thread.currentThread().getName());

        lightOn(signalType);
    }

    /**
     * Method so set the transmission rate.
     * @param unit - Integer in range 1 - 100.
     */
    public void updateSignalUnit(int unit){
        mTransmitter.setTransmissionRate(unit);
    }

    /**
     * Method to toggle light on`/off
     * @param should true to turn on or false to turn off.
     *
     * //TODO: This is getting repetitive. Consider extracting this method to the MainActivity.
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
            Log.e(mContext.getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
    }
}
