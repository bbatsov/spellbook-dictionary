package com.drowltd.spellbook.core.spellcheck;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ikkari
 *         Date: May 25, 2010
 *         Time: 8:49:20 PM
 */
public class EditsGenerator {

    private final StringBuilder builder = new StringBuilder();
    private char[] arr;
    private String word;
    private String alphabet;

    public EditsGenerator() {

    }

    public EditsGenerator recycle(String word, String alphabet) {
        builder.setLength(0);
        this.word = word;
        this.alphabet = alphabet;

        arr = new char[word.length() + 1];
        return this;
    }

    private void setGapToArr(int gap) {

        assert gap >= 0 : "gap < 0";

        zeroArr();

        boolean gapped = false;
        for (int i = 0, j = 0; i < word.length(); ++i, ++j) {


            if (i == gap && !gapped) {
                arr[j] = 0x7;
                --i;
                gapped = true;

            } else {
                arr[j] = word.charAt(i);
            }
        }
    }

    private void plainCopytoArr() {
        for (int i = 0; i < word.length(); ++i) {
            arr[i] = word.charAt(i);
        }
    }

    private void swapChars(int c0, int c1) {
        plainCopytoArr();
        char t = arr[c0];
        arr[c0] = arr[c1];
        arr[c1] = t;
    }

    private void removeCharFromArr(int index) {
        zeroArr();
        for (int i = 0; i < word.length(); ++i) {
            if (index == i) {
                arr[i] = 0x7;
            } else {
                arr[i] = word.charAt(i);
            }
        }
    }

    private void zeroArr() {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = 0x7;

        }
    }

    private String build() {
        if (builder.length() > 0) {
            builder.delete(0, builder.length());
        }
        for (char c : arr) {

            if (c != 0x7) {

                builder.append(c);
            }
        }

        return builder.toString();
    }

    public List<String> generate() {

        ArrayList<String> result = new ArrayList<String>();
        // Deletion
        for (int i = 0; i < word.length(); ++i) {
            removeCharFromArr(i);
            result.add(build());
        }

        // Transposition
        for (int i = 0; i < word.length() - 1; ++i) {

            swapChars(i, i + 1);
            result.add(build());
        }

        // Alternation
        for (int i = 0; i < word.length(); ++i) {
            removeCharFromArr(i);
            for (char c = alphabet.charAt(0); c <= alphabet.charAt(alphabet.length() - 1); ++c) {
                arr[i] = c;
                result.add(build());
            }
        }
        // Insertion
        for (int i = 0; i <= word.length(); ++i) {
            setGapToArr(i);
            for (char c = alphabet.charAt(0); c <= alphabet.charAt(alphabet.length() - 1); ++c) {
                arr[i] = c;
                result.add(build());
            }
        }
        return result;
    }
}