package com.drowltd.spellbook.core.model;

import com.drowltd.spellbook.core.i18n.Translator;

public enum Language {
    BULGARIAN("Bulgarian", "абвгдежзийклмнопрстуфхцчшщъьюя", "flag_bulgaria.png","bg_BG"),
    ENGLISH("English", "abcdefghijklmnopqrstuvwxyz", "flag_great_britain.png","en_US"),
    GERMAN("German", "abcdefghijklmnopqrstuvwxyz", "flag_great_britain.png","en_US");

    public static final Translator TRANSLATOR = Translator.getTranslator("Model");

    private String name;
    private String alphabet;
    private String iconName;
    private String pathToHunDictionary;

    private Language(String name, String alphabet, String iconName, String pathToHunDictionary) {
        this.name = name;
        this.alphabet = alphabet;
        this.iconName = iconName;
        this.pathToHunDictionary = pathToHunDictionary;
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

    public String getPathToHunDictionary() {
        return pathToHunDictionary;
    }

    @Override
    public String toString() {
        return TRANSLATOR.translate(name + "(Language)");
    }
}
