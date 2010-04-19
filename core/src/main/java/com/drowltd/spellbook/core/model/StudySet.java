/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Sasho
 */

@Entity(name = "StudySet")
@Table(name = "STUDY_SETS")
public class StudySet extends AbstractEntity  {

    @OneToMany(mappedBy = "studySet")
    private Set<StudySetEntry> studySetEntries = new HashSet<StudySetEntry>();

    @Column(nullable = false)
    private String name;

    public void setStudySetEntry(StudySetEntry studySetEntries) {
        this.studySetEntries.add(studySetEntries);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

     public Set<StudySetEntry> getStudySetEntries() {
        return studySetEntries;
    }

}
