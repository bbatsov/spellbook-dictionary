package com.drowltd.spellbook.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author iivalchev
 */
@Entity(name = "RevisionEntry")
@Table(name = "REVISION_ENTRY")
public class RevisionEntry extends AbstractEntity {

    
    @Column(name = "word_translation", nullable = false, length = 10000)
    private String translation;
    

    @ManyToOne
    private RemoteDictionaryEntry remoteDictionaryEntry;

    public RemoteDictionaryEntry getRemoteDictionaryEntry() {
        return remoteDictionaryEntry;
    }

    public void setRemoteDictionaryEntry(RemoteDictionaryEntry remoteDictionaryEntry) {
        this.remoteDictionaryEntry = remoteDictionaryEntry;
    }


    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }


    public DictionaryEntry toDictionaryEntry() {
        DictionaryEntry de = new DictionaryEntry();
        de.setAddedByUser(false);
        de.setWord(remoteDictionaryEntry.getWord());
        de.setTranslation(this.getTranslation());

        return de;
    }

    public RankEntry toRankEntry() {
        RankEntry re = new RankEntry();
        re.setWord(remoteDictionaryEntry.getWord());
        re.setRank(1);
        re.setLanguage(remoteDictionaryEntry.getRemoteDictionary().getFromLanguage());

        return re;
    }
}
