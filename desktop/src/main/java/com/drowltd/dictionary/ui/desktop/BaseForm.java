package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.i18n.Translator;

import javax.swing.JComponent;

/**
 * User: bozhidar
 * Date: Oct 31, 2009
 * Time: 10:17:57 AM
 */
public abstract class BaseForm {
    private Translator translator = new Translator(this.getClass().getSimpleName());

    public abstract JComponent getComponent();

    public Translator getTranslator() {
        return translator;
    }
}
