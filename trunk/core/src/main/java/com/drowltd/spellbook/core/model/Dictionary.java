/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String iconName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public Set<DictionaryEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }
}
