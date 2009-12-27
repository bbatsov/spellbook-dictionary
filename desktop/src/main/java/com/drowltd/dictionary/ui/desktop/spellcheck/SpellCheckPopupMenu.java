package com.drowltd.dictionary.ui.desktop.spellcheck;

import com.drowltd.dictionary.core.spellcheck.SpellChecker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iivalchev
 * @since 0.2
 */
public class SpellCheckPopupMenu extends JPopupMenu {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellCheckPopupMenu.class);
    private static SpellCheckPopupMenu INSTANCE;
    private static final IntegerComparator comparator = new IntegerComparator();
    
    private final MisspelledWordsRegistry registry = MisspelledWordsRegistry.getInstance();
    private final SpellCheckFrame spellCheckFrame;
    private MisspelledWord misspelledWord;
    private final JTextPane invokerTextPane;
    private final JMenuItem noCorrectionsItem;
    private final List<CorrectionItem> correctionItems = new LinkedList<CorrectionItem>();
    private final List<JMenuItem> commonItems = new LinkedList<JMenuItem>();
    private int cursorPosition = -1;

    public static SpellCheckPopupMenu init(SpellCheckFrame spellCheckFrame) {
        if (spellCheckFrame == null) {
            LOGGER.error("spellCheckFrame is null");
            throw new NullPointerException("spellCheckFrame is null");
        }

        INSTANCE = new SpellCheckPopupMenu(spellCheckFrame);
        return INSTANCE;
    }

    public static SpellCheckPopupMenu getInstance() {
        if (INSTANCE == null) {
            throw new NullPointerException("SpellCheckPopupMenu init() should be called first");
        }
        return INSTANCE;
    }

    private SpellCheckPopupMenu(SpellCheckFrame spellCheckFrame) {

        this.noCorrectionsItem = new JMenuItem("no corrections");
        this.noCorrectionsItem.setEnabled(false);

        this.spellCheckFrame = spellCheckFrame;
        this.invokerTextPane = spellCheckFrame.getjTextPane();

        setInvoker(invokerTextPane);

        initCommonItems();

    }

    public void show(MouseEvent mouseEvent) {
        if (mouseEvent == null) {
            LOGGER.error("mouseEvent is null");
            throw new NullPointerException("mouseEvent is null");
        }

        LOGGER.info("Popup menu triggered index at: " + cordsToCursor(mouseEvent));

        removeAll();

        synchronized (registry) {
            misspelledWord = registry.getMisspelledWord(cordsToCursor(mouseEvent));
        }
        if (misspelledWord != null) {
            addCorrectionsItems();
        }

        addCommonItems();

        setLocation(mouseEvent.getLocationOnScreen());
        this.setVisible(true);
    }

    private int cordsToCursor(MouseEvent mouseEvent) {
        final int point = invokerTextPane.viewToModel(mouseEvent.getPoint());
        cursorPosition = (point > -1) ? point : 0;
        return cursorPosition;
    }

    private synchronized void addCorrectionsItems() {
        SpellChecker spellChecker = SpellChecker.getInstance();

        if (misspelledWord == null) {
            return;
        }
        
        final Map<Integer, String> corrections = spellChecker.correct(misspelledWord.getWord());

        if (corrections.isEmpty()) {
            add(noCorrectionsItem);
            return;
        }

        List<Integer> keyList = new LinkedList<Integer>(corrections.keySet());
        Collections.sort(keyList, comparator);

        for (Integer i : keyList) {
            final CorrectionItem correctionItem = new CorrectionItem(corrections.get(i));
            add(correctionItem);
            correctionItems.add(correctionItem);
        }

        add(new JPopupMenu.Separator());

    }

    private void addCommonItems() {
        for (JMenuItem item : commonItems) {
            add(item);
        }
    }

    private final void initCommonItems() {

        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                invokerTextPane.cut();
            }
        });
        commonItems.add(cutItem);

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                invokerTextPane.copy();
            }
        });
        commonItems.add(copyItem);

        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                invokerTextPane.paste();
            }
        });
        commonItems.add(pasteItem);
    }

    private class CorrectionItem extends JMenuItem {

        private String correction;

        public CorrectionItem(String correction) {
            if (correction == null) {
                LOGGER.error("correction is null");
                throw new NullPointerException("correction is null");
            }

            if (correction.isEmpty()) {
                LOGGER.error("correction is empty");
                throw new IllegalArgumentException("correction is empty");
            }

            this.correction = correction;
            this.setText(correction);
            this.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    spellCheckFrame.correct(CorrectionItem.this.correction, misspelledWord, cursorPosition);
                    synchronized (registry) {
                        registry.corrected(misspelledWord);
                    }
                }
            });
        }
    }

    private static class IntegerComparator implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            if (o1.compareTo(o2) > 0) {
                return -1;
            } else if (o1.compareTo(o2) < 0) {
                return 1;
            }
            return 0;
        }
    }
}
