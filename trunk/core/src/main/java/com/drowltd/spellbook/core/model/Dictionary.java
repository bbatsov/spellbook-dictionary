package com.drowltd.spellbook.core.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author bozhidar
 */
@Entity(name = "Dictionary")
@Table(name = "DICTIONARIES")
@NamedQueries({
    @NamedQuery(name = "Dictionary.getAllDictionaries", query = "select d from Dictionary as d")
})
public class Dictionary extends AbstractEntity {

    @OneToMany(mappedBy = "dictionary", fetch = FetchType.LAZY)
    private Set<DictionaryEntry> dictionaryEntries = new HashSet<DictionaryEntry>();

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "from_language")
    private Language fromLanguage;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "to_language")
    private Language toLanguage;
    
    @Column(name = "icon_name", nullable = false)
    private String iconName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public Set<DictionaryEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (o.getClass() != this.getClass()) {
            return false;
        }

        Dictionary other = (Dictionary) o;

        if (name.equals(other.name)) {
            return false;
        }
        if (fromLanguage != other.fromLanguage) {
            return false;
        }
        if (toLanguage != other.toLanguage) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 73 * hash + (this.fromLanguage != null ? this.fromLanguage.hashCode() : 0);
        hash = 73 * hash + (this.toLanguage != null ? this.toLanguage.hashCode() : 0);
        return hash;
    }
}
