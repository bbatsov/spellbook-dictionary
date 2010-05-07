package com.drowltd.spellbook.ui.desktop.exam;

import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Difficulty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Bozhidar Batsov
 * @since 0.3
 */
public class ExamStats {
    private Difficulty difficulty;
    private Dictionary dictionary;
    private List<String> incorrectWords = new ArrayList<String>();
    private List<String> correctWords = new ArrayList<String>();
    private Date startTime = new Date();
    private Date endTime;

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public List<String> getIncorrectWords() {
        return incorrectWords;
    }

    public void setIncorrectWords(List<String> incorrectWords) {
        this.incorrectWords = incorrectWords;
    }

    public List<String> getCorrectWords() {
        return correctWords;
    }

    public void setCorrectWords(List<String> correctWords) {
        this.correctWords = correctWords;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getTotalWords() {
        return correctWords.size() + incorrectWords.size();
    }
}
