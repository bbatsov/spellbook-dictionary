package com.drowltd.spellbook.core.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author bozhidar
 */
@Entity(name = "DictionaryEntry")
@Table(name = "DICTIONARY_ENTRIES")
public class DictionaryEntry extends AbstractEntity {
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Dictionary dictionary;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "dictionaryEntry", fetch=FetchType.LAZY)
    private Set<StudySetEntry> dictionaryEntries = new HashSet<StudySetEntry>();

    @Column(nullable=false, unique=true)
    private String word;
    @Column(name="word_translation", nullable=false, length=10000)
    private String translation;

    @Column(name = "added_by_user", nullable=false)
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


    public Set<StudySetEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }

    public void setDictionaryEntries(Set<StudySetEntry> dictionaryEntries) {
        this.dictionaryEntries = dictionaryEntries;
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
        if ((this.translation == null) ? (other.translation != null) : !this.translation.equals(other.translation)) {
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
