package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.drowltd.spellbook.ui.swing.util.SwingUtil;
import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.ButtonPanel;
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
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The word of the day dialog displays random words selected from the default dictionary.
 *
 * @author Bozhidar Batsov
 * @since 0.3
 */
public class WordOfTheDayDialog extends BaseDialog {
    private static final DictionaryService DICTIONARY_SERVICE = DictionaryService.getInstance();

    private List<String> words;
    private Dictionary dictionary;
    private List<String> wordsShown = new ArrayList<String>();
    private int currentIndex;
    private JTextPane translationPane;
    private BannerPanel bannerPanel;

    private static final int MINIMUM_WIDTH = 600;
    private static final int MINIMUM_HEIGHT = 300;
    private static final int BANNER_PANEL_FONT_SIZE = 12;

    public WordOfTheDayDialog(Frame owner, List<String> words, Dictionary dictionary) throws HeadlessException {
        super(owner, true);

        this.words = words;
        this.dictionary = dictionary;

        setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
    }

    @Override
    public JComponent createBannerPanel() {
        bannerPanel = new BannerPanel(getTranslator().translate("Banner(Header)"),
                "",
                IconManager.getImageIcon("lightbulb_on.png", IconManager.IconSize.SIZE32));
        bannerPanel.setFont(new Font("Tahoma", Font.PLAIN, BANNER_PANEL_FONT_SIZE));
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
        ButtonPanel buttonPanel = new ButtonPanel(SwingConstants.RIGHT);

        final JButton previousButton = new JButton();

        previousButton.setAction(new AbstractAction(getTranslator().translate("Previous(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentIndex > 0) {
                    showPreviousWord();
                }

                // the index was changed in showPrevious word
                if (currentIndex == 0) {
                    previousButton.getAction().setEnabled(false);
                }
            }
        });

        JButton nextButton = new JButton();
        nextButton.setAction(new AbstractAction(getTranslator().translate("Next(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNextWord();

                // obviously at this point we can go back
                previousButton.getAction().setEnabled(true);
            }
        });

        JButton closeButton = createCloseButton();

        buttonPanel.addButton(previousButton);
        buttonPanel.addButton(nextButton);
        buttonPanel.addButton(closeButton);

        setDefaultCancelAction(closeButton.getAction());
        setDefaultAction(previousButton.getAction());
        getRootPane().setDefaultButton(nextButton);

        previousButton.getAction().setEnabled(false);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    private void showNextWord() {
        String word;

        if (wordsShown.isEmpty() || (currentIndex == (wordsShown.size() - 1))) {
            word = words.get(new Random().nextInt(words.size()));
            wordsShown.add(word);
            // index must be set to last word
            currentIndex = wordsShown.size() - 1;
        } else {
            word = wordsShown.get(++currentIndex);
        }

        showWord(word);
    }

    private void showPreviousWord() {
        String word = wordsShown.get(--currentIndex);

        showWord(word);
    }

    private void showWord(String word) {
        bannerPanel.setSubtitle(getTranslator().translate("Banner(Message)", word));

        translationPane.setText(SwingUtil.formatTranslation(word, DICTIONARY_SERVICE.getTranslation(word, dictionary)));
        translationPane.setCaretPosition(0);
    }
}
