/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ScoreboardEntry")
@Table(name = "SCOREBOARD")
/**
 *
 * @author Snow
 */
public class ScoreboardEntry extends AbstractEntity {

    @Column(name = "exam_words")
    private Double examWords;
    @Column(name = "wrong_word")
    private Double wrongWords;
    private String username;
    private String difficulty;

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setExamWords(Double examWords) {
        this.examWords = examWords;
    }

    public Double getExamWords() {
        return examWords;
    }

    public void setWrongWords(Double wrongWords) {
        this.wrongWords = wrongWords;
    }

    public Double getWrongWords() {
        return wrongWords;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return difficulty;
    }
}