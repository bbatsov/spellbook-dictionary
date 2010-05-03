package com.drowltd.spellbook.core.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author iivalchev
 */
@Entity(name = "RemoteDictionaryEntry")
@Table(name = "REMOTE_DICTIONARY_ENTRY")
@NamedQueries({
@NamedQuery(name = "RemoteDictionaryEntry.getLastRevision",
query = "select re from RevisionEntry re where re.created = (select max(re.created) from re) and re.remoteDictionaryEntry = :remoteDictionaryEntry"),
@NamedQuery(name="RemoteDictionaryEntry.getRemoteDictionaryEntry", query="select rde from RemoteDictionaryEntry rde where rde.word = :word")})
public class RemoteDictionaryEntry extends AbstractEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private RemoteDictionary remoteDictionary;

    @Column(name="word", nullable = false, unique = true)
    private String word;

    @OneToMany(mappedBy = "remoteDictionaryEntry", fetch = FetchType.LAZY)
    private Set<RevisionEntry> revisions = new HashSet<RevisionEntry>();

    public RemoteDictionaryEntry() {
    }

    public RemoteDictionary getRemoteDictionary() {
        return remoteDictionary;
    }

    public void setRemoteDictionary(RemoteDictionary remoteDictionary) {
        this.remoteDictionary = remoteDictionary;
    }

    public Set<RevisionEntry> getRevisions() {
        return revisions;
    }

    public void setRevisions(Set<RevisionEntry> revisions) {
        this.revisions = revisions;
    }

    public void addRevision(RevisionEntry entry) {
        if (entry == null) {
            return;
        }

        revisions.add(entry);
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public boolean equals(Object o){
        if(o == null)
        return false;

        if(this == o) return true;

        if(o.getClass() != RemoteDictionaryEntry.class) return false;

        RemoteDictionaryEntry other = (RemoteDictionaryEntry) o;

        if(word.equals(other.word) && remoteDictionary == other.remoteDictionary) return true;

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.remoteDictionary != null ? this.remoteDictionary.hashCode() : 0);
        hash = 29 * hash + (this.word != null ? this.word.hashCode() : 0);
        return hash;
    }
    
}
