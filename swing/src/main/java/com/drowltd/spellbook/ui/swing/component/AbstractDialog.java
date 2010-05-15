package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.i18n.Translator;
import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.icons.JideIconsFactory;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public abstract class AbstractDialog extends StandardDialog {
    private static final int FONT_SIZE = 11;

    public AbstractDialog(JFrame parent, boolean modal) {
        super(parent, modal);
    }

    @Override
    public JComponent createBannerPanel() {
        BannerPanel bannerPanel = new BannerPanel(getTranslator().translate("BannerTitle(Message)"),
                getTranslator().translate("Banner(Message)"),
                JideIconsFactory.getImageIcon("/icons/48x48/pencil.png"));
        bannerPanel.setFont(new Font("Tahoma", Font.PLAIN, FONT_SIZE));
        bannerPanel.setBackground(Color.WHITE);
        return bannerPanel;
    }

    protected abstract Translator getTranslator();
}