package com.drowltd.spellbook.core.model;

import com.drowltd.spellbook.core.i18n.Translator;

public enum Language {
    BULGARIAN("Bulgarian", "абвгдежзийклмнопрстуфхцчшщъьюя", "flag_bulgaria.png"),
    ENGLISH("English", "abcdefghijklmnopqrstuvwxyz", "flag_great_britain.png");

    public static final Translator TRANSLATOR = Translator.getTranslator("Model");

    private String name;
    private String alphabet;
    private String iconName;

    private Language(String name, String alphabet, String iconName) {
        this.name = name;
        this.alphabet = alphabet;
        this.iconName = iconName;
    }

    public String getName() {
        return name;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public String getIconName() {
        return iconName;
    }

    @Override
    public String toString() {
        return TRANSLATOR.translate(name + "(Language)");
    }
}
