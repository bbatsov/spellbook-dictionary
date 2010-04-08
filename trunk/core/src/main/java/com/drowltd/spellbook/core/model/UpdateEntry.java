package com.drowltd.spellbook.core.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author iivalchev
 */
@Entity(name = "UpdateEntry")
@Table(name = "UPDATE_ENTRY")
@NamedQueries({
    @NamedQuery(name = "UpdateEntry.checkForUpdates",
    query = "select ue from UpdateEntry ue where ue.created > :date"),
    @NamedQuery(name = "UpdateEntry.getRemoteEntries",
    query = "select rde from RemoteDictionaryEntry rde where rde.updateEntry = :updateEntry")})
public class UpdateEntry extends AbstractEntity {

    @OneToMany(mappedBy = "updateEntry", fetch = FetchType.EAGER)
    private Set<RemoteDictionaryEntry> remoteDictionaryEntries = new HashSet<RemoteDictionaryEntry>();

    public UpdateEntry() {
    }

    public Set<RemoteDictionaryEntry> getRemoteDictionaryEntries() {
        return remoteDictionaryEntries;
    }
}
