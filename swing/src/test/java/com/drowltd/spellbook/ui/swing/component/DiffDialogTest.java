/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.ui.swing.component.DiffDialog.LCSFinder;
import com.drowltd.spellbook.ui.swing.component.DiffDialog.StringLine;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ikkari
 */
@Ignore
public class DiffDialogTest {

    public DiffDialogTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testLcs() {
    }

    @Test
    public void testHighlightPane() {
        String text0 = "It is a long established fact that a reader will\n be distracted by the readable content of a page when\n looking at its layout. The point of using\n Lorem Ipsum is that it has a more-or-less\n normal distribution of letters, as opposed to using\n 'Content here, content here', making it look like readable English.\n Many desktop publishing packages and web page editors\n now use Lorem Ipsum as their default model text,\n and a search for 'lorem ipsum' will uncover many web sites still\n in their infancy. Various versions have evolved\n over the years, sometimes by accident, sometimes on purpose\n (injected humour and the like).";
        String text1 = "It is a long establishedd fact that a reader will\n be distracted by the readable content of a page when\n lookingat its layout. The point of using\n Lorem Ipsum is that it has a more-or-less\n normal distribution of letters, opposed to using\n 'Content here, content here', making it look like readable English.\n Many desktop publishing packages and web page editors\n now use Lorem Ipsum as their default model text,\n and a search for 'lorem ipsum' will uncover many web sites still\n in their infancy. Various versions have evolved\n over the years, sometimes by accident, sometimes on purpose\n (injected humour and the like)";

        new DiffDialog(null, true).diff(text0, text1).setVisible(true);
    }

    //@Test
    public void testBreakIntoLines() {
        String text = "aaaa\nbbbb";

        String seq0 ="aaaa";
        String seq1 ="bbbb";
        StringLine stringLine0 = new DiffDialog.StringLine(seq0, 0, seq0.length());
        StringLine stringLine1 = new DiffDialog.StringLine(seq1, seq0.length() - 1, seq1.length());

        List<StringLine> expected = new ArrayList<StringLine>();
        expected.add(stringLine0);
        expected.add(stringLine1);

        assertEquals("lines doesn't match",new DiffDialog(null, true).diff(text, text).breakIntoLines(text), expected);
    }

    //@Test
    public void testLCS(){

        String seq0 ="aaaa";
        String seq1 ="bbbb";
        String seq2 ="cccc";
        StringLine stringLine0 = new DiffDialog.StringLine(seq0, 0, seq0.length());
        StringLine stringLine1 = new DiffDialog.StringLine(seq1, seq0.length() - 1, seq1.length());
        StringLine stringLine2 = new DiffDialog.StringLine(seq2, seq1.length() - 1, seq2.length());

        List<StringLine> base = new ArrayList<StringLine>();
        List<StringLine> remote = new ArrayList<StringLine>();
        List<StringLine> expected = new ArrayList<StringLine>();

        base.add(stringLine0);
        base.add(stringLine1);
        base.add(stringLine2);

        remote.add(stringLine0);
        remote.add(stringLine2);
        remote.add(stringLine0);

        expected.add(stringLine0);
        expected.add(stringLine2);

        LCSFinder lCSFinder = new DiffDialog.LCSFinder(base, remote);
        assertEquals("sequences doesn't match",lCSFinder.find(), expected);
    }

}