package com.drowltd.spellbook.core.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author iivalchev
 */
@Entity(name = "RemoteDictionary")
@Table(name = "REMOTE_DICTIONARY")
@NamedQueries(
@NamedQuery(name = "RemoteDictionary.gerRemoteDictionaries",
query = "select rde from RemoteDictionary rde"))
public class RemoteDictionary extends AbstractEntity {

    @Column(nullable = false)
    private String name;
    @OneToMany(mappedBy = "remoteDictionary", fetch=FetchType.LAZY)
    private Set<RemoteDictionaryEntry> remoteDictionaryEntries = new HashSet<RemoteDictionaryEntry>();
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "from_language")
    private Language fromLanguage;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "to_language")
    private Language toLanguage;

    public RemoteDictionary() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<RemoteDictionaryEntry> getRemoteDictionaryEntries() {
        return remoteDictionaryEntries;
    }

    public void setRemoteDictionaryEntries(Set<RemoteDictionaryEntry> remoteDictionaryEntries) {
        this.remoteDictionaryEntries = remoteDictionaryEntries;
    }

    public Language getFromLanguage() {
        return fromLanguage;
    }

    public void setFromLanguage(Language fromLanguage) {
        this.fromLanguage = fromLanguage;
    }

    public Language getToLanguage() {
        return toLanguage;
    }

    public void setToLanguage(Language toLanguage) {
        this.toLanguage = toLanguage;
    }
}
