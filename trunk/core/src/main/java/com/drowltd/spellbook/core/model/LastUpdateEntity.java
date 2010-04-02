
package com.drowltd.spellbook.core.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author iivalchev
 */
@Entity(name = "LastUpdateEntity")
@Table(name = "LAST_UPDATE")
public class LastUpdateEntity extends AbstractEntity {
    public LastUpdateEntity(){
    }

}
