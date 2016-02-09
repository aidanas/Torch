package com.aidanas.torch.morsetools;

import android.util.Log;

import com.aidanas.torch.Const;

import java.util.HashMap;
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
            {true, true, false, false},     // Z

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

    public static Map<Character, boolean[]> alphabeth = new HashMap<>();

    static {

        // Add letters.
        for (char ch = 'A' ; ch <= 'Z' ; ch++){
            if (Const.DEBUG) Log.v(TAG, "Generating Morse dictionary " + ch);
            alphabeth.put(ch, moLettersArr[ch - 'A']);
        }

        // Add numbers.
        for (char ch = '1' ; ch <= 'Z' ; ch++){
            if (Const.DEBUG) Log.v(TAG, "Generating Morse dictionary " + ch);
            alphabeth.put(ch, moLettersArr[ch - 'A']);
        }

    }
}
