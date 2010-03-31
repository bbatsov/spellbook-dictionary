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
@Entity(name = "RemoteDictionaryEntry")
@Table(name = "REMOTE_DICTIOONARY_ENTRY")
public class RemoteDictionaryEntry extends AbstractEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dictionary dictionary;
    @Column(nullable = false, unique = true)
    private String word;
    @Column(name = "word_translation", nullable = false, length = 10000)
    private String translation;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private UpdateEntry updateEntry;

    public RemoteDictionaryEntry() {
    }

    public UpdateEntry getUpdateEntry() {
        return updateEntry;
    }

    public void setUpdateEntry(UpdateEntry updateEntry) {
        this.updateEntry = updateEntry;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public DictionaryEntry toDictionaryEntry() {
        DictionaryEntry de = new DictionaryEntry();
        de.setAddedByUser(false);
        de.setWord(this.getWord());
        de.setTranslation(this.getTranslation());

        return de;
    }
}
