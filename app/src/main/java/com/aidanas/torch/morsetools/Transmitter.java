package com.aidanas.torch.morsetools;

import android.util.Log;

import com.aidanas.torch.Const;

import java.util.List;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 09/02/2016
 *
 * Class to model a Morse code transmitter. It uses Camera and a list of MoLetters to signal. Note
 * that once started there are no means to stop the transmission other than interrupt the thread in
 * which it is run. Therefore it should not be run on UI thread.
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

    // Starting index of the transmission.
    private int mOffset = 0;

    // Sets to true if it is currently in the process of signalling.
    private volatile boolean mIsTransmitting = false;


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

        MoLetter moLtr;
        boolean[] mLtr;

        // Initialise loop counter outside the loop as it must be reset after each full loop.
        int i = (mOffset == SignalReceiver.CLEAR_INDEX) ? 0 : mOffset ;

        // Loop indefinitely.
        while (true) {

            // Set the mode to 'Transmitting...'
            mIsTransmitting = true;

            // For every letter in the Morse code string...
            for ( ; i < moTxt.size() ; i++){
                moLtr = moTxt.get(i);
                mLtr  = moLtr.getMoLetter();

                receiver.updateCurrentIndex(i);

                // For every dot/dash of this letter...
                for (int j = 0 ; j < mLtr.length ; j++ ) {

                    receiver.signal(SIGNAL_ON);

                    // Keep the light on for a DASH (3 units) or a DOT (1 unit).
                    Thread.sleep(mLtr[j] ? DASH_MULTIPLIER * mBase : mBase);

                    receiver.signal(SIGNAL_OFF);

                /*
                 * Signal off duration between signals of THE SAME LETTER is 1 unit.
                 * Ignore if it is the last signal of this letter.
                 */
                    if (j < mLtr.length - 1) {
                        if (Const.DEBUG) Log.v(TAG, "SAME LETTER pause!");
                        Thread.sleep(mBase);
                    }
                }

                /*
                 * Pause between letters or words or if its an end of the string.
                 * Note: Last letter of a word does not fall into eny of the cases. It is handled
                 * on the next iteration of the loop when (getChar() == ' ') becomes true.
                 */
                if (moLtr.getChar() == ' '){
                    if (Const.DEBUG) Log.v(TAG, "WORD pause!");
                    Thread.sleep(PAUSE_BETWEEN_WORDS * mBase);
                } else if (i >= moTxt.size() - 1){
                    if (Const.DEBUG) Log.v(TAG, "WORD pause, End of String!");
                    receiver.updateCurrentIndex(SignalReceiver.CLEAR_INDEX);
                    Thread.sleep(PAUSE_BETWEEN_WORDS * mBase);
                } else if (moTxt.get(i+1).getChar() != ' '){
                    if (Const.DEBUG) Log.v(TAG, "CHAR pause!");
                    Thread.sleep(PAUSE_BETWEEN_CHARS * mBase);
                }
            }

            i = 0; // From now on, loop from the beginning.
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

    /**
     * Method to set the starting position of the text to be transmitted. If none is set then the
     * default value of 0 is used indicating that the transmission should begin at the beginning of
     * the string.
     * @param offset - Starting position of the transmission. If value supplied is greater than the
     *               number of letters in the string then it is ignored and the default value used
     *               instead.
     */
    protected void setOffset(int offset){
        if (offset < moTxt.size()) this.mOffset = offset;
    }

    /**
     * Method to check if the transmitter is currently engaged in transmission.
     * @return - True if the transmitter is currently transmitting.
     */
    protected boolean isTransmitting(){
        return this.mIsTransmitting;
    }

    /***********************************************************************************************
     *                                Interface bellow
     **********************************************************************************************/

    /**
     * Interface which the class instantiating this class must implement in order to receive signals
     * representing the text in morse code.
     */
    public interface SignalReceiver {

        int CLEAR_INDEX = -1;

        /**
         * Methoid gets called every time the Morse code signalling occurs.
         * @param type - True for DASH or false for DOT.
         */
        void signal (boolean type);

        /**
         * Callback method to receive updates about the index of currently transmitting letter.
         * @param index - Index of the letter in the text.
         */
        void updateCurrentIndex(int index);

    }

}
