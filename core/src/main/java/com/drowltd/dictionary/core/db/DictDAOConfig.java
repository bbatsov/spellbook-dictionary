
package com.drowltd.dictionary.core.db;

import javax.swing.ImageIcon;

/**
 *
 * @author iivalchev
 */
public class DictDAOConfig {

    private String translationsTable;
    private String ratingsTable;

    private Language languageFrom;
    private Language languageTo;

    private ImageIcon flag;

    public Language getLanguageFrom() {
        return languageFrom;
    }

    public Language getLanguageTo() {
        return languageTo;
    }

    public String getRatingsTable() {
        return ratingsTable;
    }

    public String getTranslationsTable() {
        return translationsTable;
    }

    public ImageIcon getFlag() {
        return flag;
    }


}
