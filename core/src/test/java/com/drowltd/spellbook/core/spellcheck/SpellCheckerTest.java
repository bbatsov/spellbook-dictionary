/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.spellcheck;

import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
        
        List<String> edits = SpellChecker.edits(word, alphabet);
        List<String> edits0 = SpellChecker.edits0(word, alphabet);

        assertEquals("edits are not equal", edits, edits0);
    }

}