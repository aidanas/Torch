package com.aidanas.torch.morsetools;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

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

    // UI thread handler. Used to update views on the main thread.
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    // View, which will receive updates with currently transmitting index.
    private TextView mTextTransmitting;

    // Index of current letter. Read by UI thread, written by background.
    private volatile int mCurrentIndex;

    /**
     * Constructor.
     * @param context - Context in which this thread will be run.
     * @param cam - Device camera object which flash will be used for signaling Morse code.
     * @param txtInMorse - List of MoLetter objects.
     * @param textView - TextView to be updated while transmitting.
     */
    public TransmissionThread(Context context, Camera cam, List<MoLetter> txtInMorse,
                              TextView textView){
        this.mContext = context;
        this.mCam = cam;
        this.mTextTransmitting = textView;
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
            lightOn(false);
        }
    }

    /**
     * Method to chech if the thread's threads transmitter is currently transmitting.
     * @return - True is signalling is currently active.
     */
    public boolean isTransmitting(){
        return mTransmitter.isTransmitting();
    }

    /**
     * Method to obtain the index of currently being transmitted letter in the text.
     * @return - Index of current letter.
     */
    public int getCurrentIndex(){
        return mCurrentIndex;
    }

    /**
     * Method to indicate the required offset in the text as a beginning of the text. Especially
     * usefull if the transmission was interupted and needed to be continued from a given point in
     * text.
     *
     * @param index - The index in the text.
     */
    public void setCurrentIndex(int index){
        mTransmitter.setOffset(index);
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

    /***********************************************************************************************
     *                            Interface implementations
     **********************************************************************************************/

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
     * Receives updates about currently transmitting letter index.
     * @param index - Index of the letter in the text.
     */
    @Override
    public void updateCurrentIndex(int index) {
        mCurrentIndex = index;
        mHandler.post(new StatusUpdater(index));
    }

    /***********************************************************************************************
     *                          Inner Classes
     **********************************************************************************************/

    /**
     * Runnable class to be posted on UI thread in order to update the status view.
     */
    private class StatusUpdater implements Runnable{

        private int mmIndex;
        /**
         * Constructor.
         * @param index - Index of the letter currently transmitting.
         */
        public StatusUpdater(int index){
            this.mmIndex = index;
        }

        @Override
        public void run() {
            mTextTransmitting.setText("" + mmIndex);
        }
    }
}
