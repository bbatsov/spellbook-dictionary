/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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

    @OneToOne(optional=false, fetch=FetchType.LAZY)
    private DictionaryEntry dictionaryEntry;

    public StudySetEntry() {
    }

    public StudySet getStudySet() {
        return studySet;
    }

    public void setStudySet(StudySet studySet) {
        this.studySet = studySet;
    }

    public DictionaryEntry getDictionaryEntry(){
        return dictionaryEntry;
    }

    public void setDictionaryEntry(DictionaryEntry dictionaryEntry){
        this.dictionaryEntry = dictionaryEntry;
    }
}
