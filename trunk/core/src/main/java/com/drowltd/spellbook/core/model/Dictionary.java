package com.drowltd.spellbook.core.model;

import com.drowltd.spellbook.core.i18n.Translator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author bozhidar
 */
@Entity(name = "Dictionary")
@Table(name = "DICTIONARIES")
@NamedQueries({
    @NamedQuery(
            name = "Dictionary.getAllDictionaries",
            query = "select d from Dictionary as d"),
    @NamedQuery(
            name = "Dictionary.getDictionaryByLanguages",
            query = "select d from Dictionary d where d.fromLanguage = :fromLanguage and d.toLanguage = :toLanguage")
})
public class Dictionary extends AbstractEntity {
    public static final Translator TRANSLATOR = Translator.getTranslator("Model");

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

    @Column(nullable = false)
    private boolean special;

    @Lob
    @Column(name = "icon_small", nullable = false, columnDefinition = "varbinary")
    private byte[] iconSmall;

    @Lob
    @Column(name = "icon_big", nullable = false, columnDefinition = "varbinary")
    private byte[] iconBig;

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

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(final boolean pSpecial) {
        special = pSpecial;
    }

    public byte[] getIconSmall() {
        return iconSmall;
    }

    public void setIconSmall(final byte[] pIconSmall) {
        iconSmall = pIconSmall;
    }

    public byte[] getIconBig() {
        return iconBig;
    }

    public void setIconBig(final byte[] pIconBig) {
        iconBig = pIconBig;
    }

    public Set<DictionaryEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Dictionary that = (Dictionary) o;

        if (special != that.special) {
            return false;
        }
        if (fromLanguage != that.fromLanguage) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        if (toLanguage != that.toLanguage) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int tresult = name.hashCode();
        tresult = 31 * tresult + fromLanguage.hashCode();
        tresult = 31 * tresult + toLanguage.hashCode();
        tresult = 31 * tresult + (special ? 1 : 0);
        return tresult;
    }

    @Override
    public String toString() {
        return TRANSLATOR.translate(name + "(Dictionary)");
    }
}
