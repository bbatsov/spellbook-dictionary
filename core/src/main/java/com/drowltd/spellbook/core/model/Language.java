package com.drowltd.spellbook.core.model;

import javax.persistence.Id;

public enum Language {
    BULGARIAN("абвгдежзийклмнопрстуфхцчшщъьюя"),
    ENGLISH("abcdefghijklmnopqrstuvwxyz");

    private String alphabet;

    Language() {}

    private Language(String alphabet) {
        this.alphabet = alphabet;
    }

    public String getAlphabet() {
        return alphabet;
    }    
}
