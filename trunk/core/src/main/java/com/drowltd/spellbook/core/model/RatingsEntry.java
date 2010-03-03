package com.drowltd.spellbook.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author iivalchev
 */
@Entity(name = "RatingsEntry")
@Table(name = "RatingsEntry")
public class RatingsEntry extends AbstractEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dictionary dictionary;

    @Column(nullable = false)    
    private Language lang;
    @Column(nullable = false)
    private String word;
    @Column(nullable = false)
    private int spellcheckRank;

    public RatingsEntry() {
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public Language getLang() {
        return lang;
    }

    public void setLang(Language lang) {
        this.lang = lang;
    }

    public int getSpellcheckRank() {
        return spellcheckRank;
    }

    public void setSpellcheckRank(int spellcheckRank) {
        this.spellcheckRank = spellcheckRank;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (o.getClass() != getClass()) {
            return false;
        }

        RatingsEntry other = (RatingsEntry) o;

        if (!word.equals(other.word)) {
            return false;
        }
        if (!dictionary.equals(other.dictionary)) {
            return false;
        }
        if (lang != other.lang) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.dictionary != null ? this.dictionary.hashCode() : 0);
        hash = 67 * hash + (this.lang != null ? this.lang.hashCode() : 0);
        hash = 67 * hash + (this.word != null ? this.word.hashCode() : 0);
        return hash;
    }
}
