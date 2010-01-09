package com.drowltd.dictionary.core.db;

import javax.swing.ImageIcon;

/**
 *
 * @author iivalchev
 */
public class SDictionary {

    private final String name;
    private final Language languageFrom;
    private final Language languageTo;
    private ImageIcon flag;

    public SDictionary(String name, Language languageFrom, Language languageTo) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is null or empty");
        }

        if (languageFrom == null) {
            throw new IllegalArgumentException("languageFrom is null");
        }

        if (languageTo == null) {
            throw new IllegalArgumentException("languageTo is null");
        }

        this.name = name;
        this.languageFrom = languageFrom;
        this.languageTo = languageTo;
    }

    public ImageIcon getFlag() {
        return new ImageIcon(flag.getImage());
    }

    public Language getLanguageFrom() {
        return languageFrom;
    }

    public Language getLanguageTo() {
        return languageTo;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (o.getClass() != this.getClass()) {
            return false;
        }

        SDictionary other = (SDictionary) o;

        if (languageFrom.equals(other.languageFrom)
                && languageTo.equals(other.languageTo)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.languageFrom != null ? this.languageFrom.hashCode() : 0);
        hash = 89 * hash + (this.languageTo != null ? this.languageTo.hashCode() : 0);
        return hash;
    }
}
