package com.aidanas.torch.morsetools;

import android.util.Log;

import com.aidanas.torch.Const;

import java.util.List;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 09/02/2016
 *
 * Class to model a Morse code transmitter. It uses Camera and a list of MoLetters to signal.
 */
public class Transmitter {

    // Tag.
    public static final String TAG = Transmitter.class.getSimpleName();

    /*
     * Time length used in all calculations as the base. Each modification of mBase will use BASE as
     * the unit upn which culations will be made.
     */
    public static final long BASE = 200L;

    /*
     * Morse code frequency mBase in milliseconds. Must be initialised.
     * Needs to be atomic as it would be read/written my multiple threads.
     */
    private volatile long mBase;


    // Pause duration between words and characters in milliseconds.
    public static final long PAUSE_BETWEEN_CHARS = 3;
    public static final long PAUSE_BETWEEN_WORDS = 7;

    // Number of DOTS in a DASH.
    public static final int DASH_MULTIPLIER = 3;

    // Just for readability.
    public static final boolean SIGNAL_ON  = true;
    public static final boolean SIGNAL_OFF = false;

    // MIN/MAX values the supplied rate can have and its multiplier.
    private static final int RATE_MIN = 1;
    private static final int RATE_MAX = 100;
    private static final int RATE_MULTIPLIER = 5;

    private final List<MoLetter> moTxt;

    private final SignalReceiver receiver;


    /**
     * Two arg constructor.
     * @param receiver - Class implementing this interface will get signals of the Morse code as
     *                 callbacks.
     * @param moTxt - morse code text to be transmitted.
     */
    public Transmitter (SignalReceiver receiver, List<MoLetter> moTxt){
        this.receiver = receiver;
        this.moTxt = moTxt;
        setTransmissionRate(1); // Initial transmission rate. SLOWEST
    }

    /**
     * Method to start signalling the receiver the morse code message stored in moTxt.
     * Method loops infinitely. Does NOT return!
     */
    public void startTransmission() throws InterruptedException {

        int jMax;
        MoLetter moLtr;
        boolean[] mLtr;

        // Loop indefinitely.
        while (true) {

            // For every letter in the Morse code string...
            for (int i = 0 ; i < moTxt.size() ; i++){
                moLtr = moTxt.get(i);
                mLtr  = moLtr.getMoLetter();
                jMax = mLtr.length;

                // For every dot/dash of this letter...
                for (int j = 0 ; j < jMax ; j++ ) {

                    receiver.signal(SIGNAL_ON);

                    // Keep the light on for a DASH (3 units) or a DOT (1 unit).
                    Thread.sleep(mLtr[j] ? DASH_MULTIPLIER * mBase : mBase);

                    receiver.signal(SIGNAL_OFF);

                /*
                 * Signal off duration between signals of THE SAME LETTER is 1 unit.
                 * Ignore if it is the last signal of this letter.
                 */
                    if (j < jMax - 1) {
                        if (Const.DEBUG) Log.v(TAG, "TESTING! SAME LETTER pause!");
                        Thread.sleep(mBase);
                    }
                }

                // Pause for a letter or a word. If its the end of a string then do a word pause.
                if (moLtr.getChar() == ' ' || i >= moTxt.size() - 1){
                    if (Const.DEBUG) Log.v(TAG, "TESTING! WORD pause!");
                    Thread.sleep(PAUSE_BETWEEN_WORDS * mBase);
                } else {
                    if (Const.DEBUG) Log.v(TAG, "TESTING! CHAR pause!");
                    Thread.sleep(PAUSE_BETWEEN_CHARS * mBase);
                }
            }
        }
    }

    /**
     * Method to ajust the speed of transmission.
     * @param rate - Integer in range 1 - 100.
     */
    protected void setTransmissionRate(int rate){
        if (rate < RATE_MIN || rate > RATE_MAX) return;

        mBase = BASE + Math.abs((rate-RATE_MAX)*RATE_MULTIPLIER);
        if (Const.DEBUG) Log.v(TAG, "In setTransmissionRate() new mBase = " + mBase);

    }

    /***********************************************************************************************
     *                                Interface bellow
     **********************************************************************************************/

    /**
     * Interface which the class instantiating this class must implement in order to receive signals
     * representing the text in morse code.
     */
    public interface SignalReceiver {

        /**
         * Methoid gets called every time the Morse code signalling occurs.
         * @param type - True for DASH or false for DOT.
         */
        void signal (boolean type);

    }

}
