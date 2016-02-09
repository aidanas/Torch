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

    //TODO: necessary?

//    /**
//     * Method returns the size of the letter (number of true/false terms.)
//     * @return - Size of the letter.
//     */
//    public int getSize(){
//        return moLetter.length;
//    }

    /**
     * Getter for a Mosrse code representation of this letter.
     * @return - Morse code letter represented as an array of true/false values.
     */
    public boolean[] getMoLetter() {
        return moLetter;
    }

    /**
     * Method to obtain the ASKII char which this object represent.
     * @return - ASKII character.
     */
    public char getChar(){
        return this.ch;
    }
}
