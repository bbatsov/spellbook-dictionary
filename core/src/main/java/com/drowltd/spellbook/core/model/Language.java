package com.drowltd.spellbook.core.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author iivalchev
 */
@Entity(name="Language")
@Table(name="Language")
public class Language extends AbstractEntity {
    @OneToMany(fetch=FetchType.LAZY)
    private List<RatingsEntry> ratingsEntries = new ArrayList<RatingsEntry>();

    private String alphabet;
    private String name;

    public Language() {
    }

    public String getAlphabet() {
        return alphabet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    public List<RatingsEntry> getRatingsEntries() {
        return ratingsEntries;
    }

    @Override
    public String toString(){
        return name;
    }

}
