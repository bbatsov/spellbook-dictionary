
package com.drowltd.spellbook.core.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author iivalchev
 */

@Entity(name="Language")
@Table(name="Language")
public class Language extends AbstractEntity {
    


    private String alphabet;
    private String name;

    public Language(){
        
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

    @Override
    public String toString(){
        return name;
    }

}
