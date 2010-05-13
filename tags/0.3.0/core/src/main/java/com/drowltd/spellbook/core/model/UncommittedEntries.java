package com.drowltd.spellbook.core.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author iivalchev
 */
@Table(name = "UNCOMMITTED_ENTRIES")
@Entity(name = "UncommittedEntries")
@NamedQueries(
@NamedQuery(name = "UncommittedEntries.getUncommittedEntries",
query = "select ue from UncommittedEntries ue where ue.committed = false"))
public class UncommittedEntries extends AbstractEntity {

    @Column(name = "committed", nullable = false)
    private boolean committed;
    @OneToMany(fetch=FetchType.LAZY)
    @JoinTable(name = "UNCOMMITTED_DICTIONARY_ENTRIES",
    joinColumns =
    @JoinColumn(name = "uncommitted_id", referencedColumnName = "id"),
    inverseJoinColumns =
    @JoinColumn(name = "dictionary_entry_id", referencedColumnName = "id"))
    private Set<DictionaryEntry> dictionaryEntries = new HashSet<DictionaryEntry>();

    public UncommittedEntries() {
    }

    public void addDicionaryEntry(DictionaryEntry de) {
        if (de == null) {
            return;
        }

        dictionaryEntries.add(de);
    }

    public Set<DictionaryEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }

    public void setDictionaryEntries(Set<DictionaryEntry> dictionaryEntries) {
        this.dictionaryEntries = dictionaryEntries;
    }

    public boolean isCommitted() {
        return committed;
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }
}
