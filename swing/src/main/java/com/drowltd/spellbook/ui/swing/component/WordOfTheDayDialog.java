package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.drowltd.spellbook.ui.swing.util.SwingUtil;
import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Description goes here...
 *
 * @author Bozhidar Batsov
 * @since  0.3
 */
public class WordOfTheDayDialog extends StandardDialog {
    private static final Translator TRANSLATOR = Translator.getTranslator("WordOfTheDayDialog");
    private static final DictionaryService DICTIONARY_SERVICE = DictionaryService.getInstance();
    
    private List<String> words;
    private Dictionary dictionary;
    private List<String> wordsShown = new ArrayList<String>();
    private JTextPane translationPane;
    private BannerPanel bannerPanel;

    public WordOfTheDayDialog(List<String> words, Dictionary dictionary) throws HeadlessException {
        this.words = words;
        this.dictionary = dictionary;

        setTitle(TRANSLATOR.translate("Dialog(Title)"));

        setMinimumSize(new Dimension(600, 300));
    }

    @Override
    public JComponent createBannerPanel() {
        bannerPanel = new BannerPanel(TRANSLATOR.translate("Banner(Header)"),
               "",
                IconManager.getImageIcon("lightbulb_on.png", IconManager.IconSize.SIZE32));
        bannerPanel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        bannerPanel.setBackground(Color.WHITE);
        bannerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        return bannerPanel;
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1", "[grow]", "[grow]"));

        translationPane = new JTextPane();
        translationPane.setContentType("text/html");

        panel.add(new JScrollPane(translationPane), "grow");

        showNextWord();

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel(SwingConstants.CENTER);

        JButton previousButton = new JButton();

        previousButton.setAction(new AbstractAction(TRANSLATOR.translate("Previous(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });

        JButton nextButton = new JButton();        
        nextButton.setAction(new AbstractAction(TRANSLATOR.translate("Next(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNextWord();
            }
        });

        JButton closeButton = new JButton();
        closeButton.setAction(new AbstractAction(TRANSLATOR.translate("Close(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
                dispose();
            }
        });

        buttonPanel.addButton(previousButton);
        buttonPanel.addButton(nextButton);
        buttonPanel.addButton(closeButton);

        setDefaultCancelAction(closeButton.getAction());
        setDefaultAction(previousButton.getAction());
        getRootPane().setDefaultButton(previousButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    private void showNextWord() {
        String word = words.get(new Random().nextInt(words.size()));

        bannerPanel.setSubtitle(TRANSLATOR.translate("Banner(Message)", word));

        translationPane.setText(SwingUtil.formatTranslation(word, DICTIONARY_SERVICE.getTranslation(word, dictionary)));
        translationPane.setCaretPosition(0);
    }
}
