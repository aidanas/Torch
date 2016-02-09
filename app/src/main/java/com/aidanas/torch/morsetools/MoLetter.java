package com.aidanas.torch.morsetools;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: on 09/02/2016.
 *
 * Class to represent a single letter in morse code.
 */
public class MoLetter {

    // ASKII letter.
    private final char ch;

    // Morse code letter. Implemented as an array of bools.
    private final boolean[] moLetter;

    /**
     * Constructor.
     * @param ch - Character.
     */
    public MoLetter (char ch, boolean[]moLetter){
        this.ch = ch;
        this.moLetter = moLetter;
    }
}
