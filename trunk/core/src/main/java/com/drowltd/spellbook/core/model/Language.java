
package com.drowltd.spellbook.core.model;

/**
 *
 * @author iivalchev
 */
public enum Language {
    
    BULGARIAN("абвгдежзийклмнопрстуфхцчшщъьюя"),
    ENGLISH("abcdefghijklmnopqrstuvwxyz");

    private final String alphabet;

    private Language(String alphabet){
        this.alphabet = alphabet;
    }

    public String getAlphabet() {
        return alphabet;
    }
}
