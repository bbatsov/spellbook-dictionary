/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.spellcheck;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ikkari
 */
public class SpellCheckerTest {

    public SpellCheckerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testEdits() {
        String word = "единподобърречник";
        String alphabet = "абвгдежзийклмнопрстуфхцчшщъьюя";
        
//        List<String> edits = MapSpellChecker.edits(word, alphabet);
//        List<String> edits0 = MapSpellChecker.edits0(word, alphabet);

//        assertEquals("generate are not equal", edits, edits0);
    }

}