package com.drowltd.spellbook.ui.desktop.spellcheck;

import com.drowltd.spellbook.core.db.DatabaseService;
import com.drowltd.spellbook.core.spellcheck.SpellChecker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
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
            addToDictionoray();
            return;
        }

        List<Integer> keyList = new LinkedList<Integer>(corrections.keySet());
        Collections.sort(keyList, comparator);

        for (Integer i : keyList) {
            final CorrectionItem correctionItem = new CorrectionItem(corrections.get(i));
            add(correctionItem);
        }

        addToDictionoray();

    }

    private void addCommonItems() {
        for (JMenuItem item : commonItems) {
            add(item);
        }
    }

    private void addToDictionoray() {
        add(new JPopupMenu.Separator());
        add(new AddToDictItem(misspelledWord.getWordInLowerCase()));
        add(new JPopupMenu.Separator());
    }

    private final void initCommonItems() {

        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/cut.png")));
        cutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                invokerTextPane.cut();
            }
        });
        commonItems.add(cutItem);

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/copy.png")));
        copyItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                invokerTextPane.copy();
            }
        });
        commonItems.add(copyItem);

        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/paste.png")));
        pasteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                invokerTextPane.paste();
            }
        });
        commonItems.add(pasteItem);
    }

    private class CorrectionItem extends JMenuItem {

        public CorrectionItem(final String correction) {
            if (correction == null) {
                LOGGER.error("correction is null");
                throw new NullPointerException("correction is null");
            }

            if (correction.isEmpty()) {
                LOGGER.error("correction is empty");
                throw new IllegalArgumentException("correction is empty");
            }

            this.setText(correction);
            this.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    spellCheckFrame.correct(correction, misspelledWord, cursorPosition);
                    synchronized (registry) {
                        registry.corrected(misspelledWord);
                    }
                }
            });
        }
    }

    private class AddToDictItem extends JMenuItem {

        public AddToDictItem(final String misspelled) {
            if (misspelled == null || misspelled.isEmpty()) {
                LOGGER.error("misspelled == null || misspelled.isEmpty()");
                throw new IllegalArgumentException("misspelled == null || misspelled.isEmpty()");
            }

            this.setText("Add to Dictionary");

            this.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    DatabaseService.getInstance().addMisspelled(misspelled, SpellChecker.getInstance().getDictionary());
                    MisspelledFinder.getInstance().addUserMisspelled(misspelled);
                    MisspelledFinder.getInstance().findMisspelled(SpellCheckPopupMenu.this.spellCheckFrame.getVisibleText(), true);
                    StatusManager.getInstance().setStatus(misspelled + " added to dictionary");
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
