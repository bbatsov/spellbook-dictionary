package com.drowltd.spellbook.core.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 *
 * @author iivalchev
 */
@Entity(name = "UpdateEntry")
@Table(name = "UPDATE_ENTRY")
@NamedQueries({
    @NamedQuery(name = "UpdateEntry.checkForUpdates",
    query = "select ue from UpdateEntry ue where ue.created > :date")
    //@NamedQuery(name = "UpdateEntry.getRemoteEntries",
    //query = "select rde from RemoteDictionaryEntry rde where rde.updateEntry = :updateEntry")
})
public class UpdateEntry extends AbstractEntity {

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "UPDATED_REMOTE_DICTIONARY_ENTRIES",
    joinColumns =
    @JoinColumn(name = "update_entry_id", referencedColumnName = "id"),
    inverseJoinColumns =
    @JoinColumn(name = "remote_dictionary_entry_id", referencedColumnName = "id"))
    private Set<RemoteDictionaryEntry> remoteDictionaryEntries = new HashSet<RemoteDictionaryEntry>();

    public UpdateEntry() {
    }

    public Set<RemoteDictionaryEntry> getRemoteDictionaryEntries() {
        return remoteDictionaryEntries;
    }

    public void addRemoteDictionaryEntry(RemoteDictionaryEntry entry){
        if(entry == null) return;

        remoteDictionaryEntries.add(entry);
    }
}
