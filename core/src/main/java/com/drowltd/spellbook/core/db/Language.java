package com.drowltd.spellbook.core.db;

import javax.swing.ImageIcon;

/**
 *
 * @author iivalchev
 */
public class Language {

    private final String name;
    private final String alphabet;
    private ImageIcon flag;

    public Language(String name, String alphabet, ImageIcon flag) {

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is null or empty");
        }

        if (alphabet == null || alphabet.isEmpty()) {
            throw new IllegalArgumentException("alphabet is null or empty");
        }

        if(flag == null){
            throw new IllegalArgumentException("flag is null");
        }
        if(flag.getImage() == null){
            throw new IllegalArgumentException("flag.getImage() is null");
        }

        this.name = name;
        this.alphabet = alphabet;
        this.flag = new ImageIcon(flag.getImage());
    }

    public String getAlphabet() {
        return alphabet;
    }

    public ImageIcon getFlag() {
        return new ImageIcon(flag.getImage());
    }

    public String getName() {
        return name;
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

        Language other = (Language) o;

        if (name.equals(other.name)
                && alphabet.equals(other.alphabet)) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.alphabet != null ? this.alphabet.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString(){
        return name;
    }
}