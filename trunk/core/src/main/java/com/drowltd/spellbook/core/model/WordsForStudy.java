/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author Sasho
 */
@Entity(name = "WordsForStudy")
@Table(name = "WORDS_FOR_STUDY")
public class WordsForStudy extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private String word;
    @Column(name = "word_translation", nullable = false, length = 10000)
    private String translation;

    public WordsForStudy() {
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String wordTranslation) {
        this.translation = wordTranslation;
    }
}
