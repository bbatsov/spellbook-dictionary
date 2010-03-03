
package com.drowltd.spellbook.core.model;

/**
 *
 * @author iivalchev
 */
public enum Language {
    
    Bulgarian("абвгдежзийклмнопрстуфхцчшщъьюя"),
    English("abcdefghijklmnopqrstuvwxyz");

    private final String alphabet;

    private Language(String alphabet){
        this.alphabet = alphabet;
    }

    public String getAlphabet() {
        return alphabet;
    }
}
