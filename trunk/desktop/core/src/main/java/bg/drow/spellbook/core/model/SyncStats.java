package bg.drow.spellbook.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Contains simple statistics for a synchronization.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
@Entity
@Table(name = "SYNC_STATS")
public class SyncStats extends AbstractEntity {
    @Column(nullable = false, name = "PULLED_ENTRIES")
    private int pulledEntries;
    @Column(nullable = false, name = "PUSHED_ENTRIES")
    private int pushedEntries;

    public int getPulledEntries() {
        return pulledEntries;
    }

    public void setPulledEntries(int pulledEntries) {
        this.pulledEntries = pulledEntries;
    }

    public int getPushedEntries() {
        return pushedEntries;
    }

    public void setPushedEntries(int pushedEntries) {
        this.pushedEntries = pushedEntries;
    }
}
