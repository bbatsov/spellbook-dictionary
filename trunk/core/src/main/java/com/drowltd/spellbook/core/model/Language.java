package com.drowltd.spellbook.core.model;

public enum Language {
    BULGARIAN("Bulgarian", "абвгдежзийклмнопрстуфхцчшщъьюя", "flag_bulgaria.png"),
    ENGLISH("English", "abcdefghijklmnopqrstuvwxyz", "flag_great_britain.png");

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
}
