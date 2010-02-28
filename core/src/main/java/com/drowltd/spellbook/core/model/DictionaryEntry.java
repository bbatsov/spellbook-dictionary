/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author bozhidar
 */
@Entity(name = "DictionaryEntry")
@Table(name = "DictionaryEntry")
public class DictionaryEntry extends AbstractEntity {
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Dictionary dictionary;

    @Column(nullable=false, unique=true)
    private String word;
    @Column(nullable=false, length=10000)
    private String wordTranslation;
    @Column(nullable=true)
    private int spellcheckRank;

    @Column(nullable=false)
    private boolean addedByUser;

    public DictionaryEntry() {
    }

    public boolean isAddedByUser() {
        return addedByUser;
    }

    public void setAddedByUser(boolean addedByUser) {
        this.addedByUser = addedByUser;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
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

    public String getWordTranslation() {
        return wordTranslation;
    }

    public void setWordTranslation(String wordTranslation) {
        this.wordTranslation = wordTranslation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DictionaryEntry other = (DictionaryEntry) obj;
        if ((this.word == null) ? (other.word != null) : !this.word.equals(other.word)) {
            return false;
        }
        if ((this.wordTranslation == null) ? (other.wordTranslation != null) : !this.wordTranslation.equals(other.wordTranslation)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }


}
