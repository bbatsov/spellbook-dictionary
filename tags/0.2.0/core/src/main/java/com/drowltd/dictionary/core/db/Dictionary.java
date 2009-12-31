/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.dictionary.core.db;

/**
 *
 * @author bozhidar
 * @since 0.1
 */
public enum Dictionary {

    EN_BG(Dictionary.EN_ALPHABET, Dictionary.EN_RATINGS_TABLE, Dictionary.EN_LANGUAGE), BG_EN(Dictionary.BG_ALPHABET, Dictionary.BG_RATINGS_TABLE, Dictionary.BG_LANGUAGE);
    private static Dictionary selectedDictionary = EN_BG;
    private static final String EN_ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final String BG_ALPHABET = "абвгдежзийклмнопрстуфхцчшщъьюя";
    private static final String EN_RATINGS_TABLE = "SPELLCHECK_EN";
    private static final String BG_RATINGS_TABLE = "SPELLCHECK_BG";
    private static final String EN_LANGUAGE = "English";
    private static final String BG_LANGUAGE = "Bulgarian";
    private String alphabet;
    private String ratingsTable;
    private String language;

    private Dictionary(String alphabet, String ratingsTable, String language) {
        this.alphabet = alphabet;
        this.ratingsTable = ratingsTable;
        this.language = language;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public String getRatingsTable() {
        return ratingsTable;
    }

    public String getLanguage() {
        return language;
    }

    public static synchronized Dictionary getSelectedDictionary() {
        return selectedDictionary;
    }

    public static synchronized void setSelectedDictionary(Dictionary selectedDictionary) {
        if (selectedDictionary == null) {
            throw new NullPointerException("selectedDictionary is null");
        }
        Dictionary.selectedDictionary = selectedDictionary;
    }
}
