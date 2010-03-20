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

@Entity(name = "StudySetEntry")
@Table(name = "STUDY_ENTRIES")

/**
 *
 * @author Sasho
 */
public class StudySetEntry extends AbstractEntity {

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private StudySet studySet;

    @Column(nullable=false, unique=true)
    private String word;
    @Column(name="word_translation", nullable=false, length=10000)
    private String translation;

    @Column(name = "added_by_user", nullable=false)
    private boolean addedByUser;

    public StudySetEntry() {
    }

    public boolean isAddedByUser() {
        return addedByUser;
    }

    public void setAddedByUser(boolean addedByUser) {
        this.addedByUser = addedByUser;
    }

    public StudySet getStudySet() {
        return studySet;
    }

    public void setStudySet(StudySet studySet) {
        this.studySet = studySet;
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
