package com.drowltd.dictionary.ui.desktop;

import javax.swing.*;
import java.awt.*;

/**
 * User: bozhidar
 * Date: Sep 6, 2009
 * Time: 9:33:03 PM
 */
public class ExamFrame extends JFrame {
    public ExamFrame() throws HeadlessException {
        setSize(480, 320);
        setTitle("Exam");
        setIconImage(IconManager.getImageIcon("teacher.png", IconManager.IconSize.SIZE48).getImage());
        setContentPane(new ExamPanel().getComponent());
    }
}
