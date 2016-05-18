package com.aidanas.torch.morsetools;

import android.util.Log;

import com.aidanas.torch.Const;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 09/02/2016
 *
 * Static class to model an ASKII to morse code translator.
 */
public class MoTranslator {

    // Tag.
    public static final String TAG = MoLetter.class.getSimpleName();

    // Regex of acceptable patterns. A string must match fully if to be successfully translated.
    public static final String VALID_REGEX = "^[A-Za-z0-9\\s]+$";

    private static boolean[][] moLettersArr = {
            {false, true},                  // A
            {true, false, false},           // B
            {true, false, true, false},     // C
            {true, false, false},           // D
            {false},                        // E
            {false, false, true, false},    // F
            {true, true, false},            // G
            {false, false, false, false},   // H
            {false, false},                 // I
            {false, true, true, true},      // J
            {true, false, true},            // K
            {false, true, false, false},    // L
            {true, true},                   // M
            {true, false},                  // N
            {true, true, true},             // O
            {false, true, true, false},     // P
            {true, true, false, true},      // Q
            {false, true, false},           // R
            {false, false, false},          // S
            {true},                         // T
            {false, false, true},           // U
            {false, false, false, true},    // V
            {false, true, true},            // W
            {true, false, false, true},     // X
            {true, false, true, true},      // Y
            {true, true, false, false}};    // Z

    private static boolean[][] moNumbersArr = {
            {true, true, true, true, true},     // 0
            {false, true, true, true, true},    // 1
            {false, false, true, true, true},   // 2
            {false, false, false, true, true},  // 3
            {false, false, false, false, true}, // 4
            {false, false, false, false, false},// 5
            {true, false, false, false, false}, // 6
            {true, true, false, false, false},  // 7
            {true, true, true, false, false},   // 8
            {true, true, true, true, false}     // 9
        };

    public static final Map<Character, boolean[]> dictionary = new HashMap<>();

    static {

        // Add letters.
        for (char ch = 'A' ; ch <= 'Z' ; ch++){
            if (Const.DEBUG) Log.v(TAG, "Generating Morse letter dictionary " + ch);
            dictionary.put(ch, moLettersArr[ch - 'A']);
        }

        // Add numbers.
        for (char ch = '0' ; ch <= '9' ; ch++){
            if (Const.DEBUG) Log.v(TAG, "Generating Morse numbers dictionary " + ch);
            dictionary.put(ch, moNumbersArr[ch - '0']);
        }

        // Empty array for space.
        dictionary.put(' ', new boolean[]{});

    }

    /**
     * Static method to translate a string of text to Morse code.
     * @param txt - String to be translated.
     * @return - Text translated to Morse code. Implemented as a list of boolean arrays.
     *
     * throws - IAE if the string passed contains any unaccepted characters.
     *          Not ion rage A-Z and 0-9.
     */
    public static List<MoLetter> translateToMorse(String txt) throws IllegalArgumentException{

        List<MoLetter> moTxt = new ArrayList<>();

        char currentCh;

        for (int i = 0 ; i < txt.length() ; i++){
            currentCh = txt.charAt(i);

            // Check if character has mapping to morse.
            if (!dictionary.containsKey(currentCh)){
                throw new IllegalArgumentException("Unsupported characters in the string! ch = " +
                        currentCh);
            }

            moTxt.add(i, new MoLetter(currentCh, dictionary.get(currentCh)));
        }
        if (Const.DEBUG) Log.v(TAG, "In translateToMorse(), translated txt = \n" + moTxt);

        return moTxt;
    }

}
