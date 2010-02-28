/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author bozhidar
 */
@Entity(name="Dictionary")
@Table(name="SpellbookDictionary")
@NamedQueries({
    @NamedQuery(name="Dictionary.getAllDictionaries", query="select d from Dictionary as d")
})
public class Dictionary extends AbstractEntity {
    @OneToMany(fetch=FetchType.LAZY)
    private Set<DictionaryEntry> dictionaryEntries = new HashSet<DictionaryEntry>();

    private String name;

    @Lob
    @Column(name = "icon16", length = 10000)
    private byte[] icon16Bytes;

    @Lob
    @Column(name = "icon24", length = 10000)
    private byte[] icon24Bytes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<DictionaryEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }
}
