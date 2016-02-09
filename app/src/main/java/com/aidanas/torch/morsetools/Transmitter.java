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

    private static final int PAUSE_BETWEEN_CHARS = 3;
    private static final int PAUSE_BETWEEN_WORDS = 7;

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
     * Method to start signalling the receiver the morse code message stored in moTxt
     */
    public void startTransmission() throws InterruptedException {

        int unit = receiver.signalUnitSize();
        int jMax;
        MoLetter moLtr;
        boolean[] mLtr;

        // For every letter in the Morse code string...
        for (int i = 0 ; i < moTxt.size() ; i++){
            moLtr = moTxt.get(i);
            mLtr  = moLtr.getMoLetter();
            jMax = mLtr.length;

            // For every dot/dash oth this letter...
            for (int j = 0 ; j < jMax ; j++ ) {
                receiver.signal(mLtr[j]);
                Thread.sleep(unit);
            }

            // Pause for a letter or a word.
            if (moLtr.getChar() == ' '){
                Thread.sleep(unit * PAUSE_BETWEEN_WORDS);
            } else {
                Thread.sleep(unit * PAUSE_BETWEEN_CHARS);
            }
        }
    }

    /***********************************************************************************************
     *                                Interface bellow
     **********************************************************************************************/

    /**
     * Interface which the class instantiating this class must implement in order to receive signals
     * representing the text in morse code.
     */
    interface SignalReceiver {

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
