package com.aidanas.torch.morsetools;

import android.os.Looper;

import java.util.List;
import java.util.logging.Handler;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 09/02/2016
 *
 * Class to model a Morse code transmitter. It uses Camera and a list of MoLetters to signal.
 */
public class Transmitter {

    // Morse code frequency base in milliseconds.
    private static final long BASE = 500L;

    // Pause duration between words and characters in milliseconds.
    public static final long PAUSE_BETWEEN_CHARS = 3 * BASE;
    public static final long PAUSE_BETWEEN_WORDS = 7 * BASE;

    // Number of DOTS in a DASH.
    public static final int DASH_MULTIPLIER = 3;

    // Just for readability.
    public static final boolean SIGNAL_ON  = true;
    public static final boolean SIGNAL_OFF = false;



    private final List<MoLetter> moTxt;

    private final SignalReceiver receiver;

    private int transmissionRate;

    /**
     * Two arg constructor.
     * @param receiver - Class implementing this interface will get signals of the Morse code as
     *                 callbacks.
     * @param moTxt - morse code text to be transmitted.
     */
    public Transmitter (SignalReceiver receiver, List<MoLetter> moTxt){
        this.receiver = receiver;
        this.moTxt = moTxt;
    }

    /**
     * Method to start signalling the receiver the morse code message stored in moTxt.
     * Method loops infinitely. Does NOT return!
     */
    public void startTransmission() throws InterruptedException {

        long unit = receiver.signalUnitSize();
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
                    Thread.sleep(mLtr[j] ? unit * DASH_MULTIPLIER * BASE : unit * BASE);

                    receiver.signal(SIGNAL_OFF);

                /*
                 * Signal off duration between signals of THE SAME LETTER is 1 unit.
                 * Ignore if it is the last signal of this letter.
                 */
                    if (j < jMax) Thread.sleep(unit * BASE);
                }

                // Pause for a letter or a word.
                if (moLtr.getChar() == ' '){
                    Thread.sleep(unit * PAUSE_BETWEEN_WORDS);
                } else {
                    Thread.sleep(unit * PAUSE_BETWEEN_CHARS);
                }
            }
            // Pause between words and repeat the whole text again.
            Thread.sleep(unit * PAUSE_BETWEEN_WORDS);
        }
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

        /**
         * The method will be called frequentlly to obtain the size of the transmission unit.
         * @return - Length of a single transmission unit.
         */
        int signalUnitSize();
    }

}
