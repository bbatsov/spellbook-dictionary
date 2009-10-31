package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.i18n.Translator;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;

/**
 * User: bozhidar
 * Date: Sep 6, 2009
 * Time: 9:33:03 PM
 */
public class ExamFrame extends JFrame {
    private static final Translator TRANSLATOR = new Translator("SpellbookApp");

    public ExamFrame() throws HeadlessException {
        //dynamically determine an adequate frame size
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        Dimension screenSize = toolkit.getScreenSize();

        setSize(screenSize.width / 3, screenSize.height / 3);
        setLocationByPlatform(true);

        //set the frame title
        setTitle(TRANSLATOR.translate("Exam(Title)"));

        setIconImage(IconManager.getImageIcon("teacher.png", IconManager.IconSize.SIZE48).getImage());
        setContentPane(new ExamForm().getComponent());
    }
}
