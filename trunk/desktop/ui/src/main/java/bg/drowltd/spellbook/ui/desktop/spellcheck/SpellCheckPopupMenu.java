package bg.drowltd.spellbook.ui.desktop.spellcheck;

import bg.drowltd.spellbook.ui.swing.util.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * @author iivalchev
 * @since 0.2
 */
public class SpellCheckPopupMenu extends JPopupMenu {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellCheckPopupMenu.class);
    private static SpellCheckPopupMenu INSTANCE;

    private MisspelledWord misspelledWord;
    private SpellCheckTab spellCheckTab;
    private JTextPane invokerTextPane;
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

/*        this.spellCheckFrame = spellCheckFrame;
        this.invokerTextPane = spellCheckFrame.getjTextPane();

        setInvoker(invokerTextPane);*/

        initCommonItems();

    }

    public SpellCheckPopupMenu(SpellCheckTab spellCheckTab){
        this.spellCheckTab = spellCheckTab;
        this.invokerTextPane = spellCheckTab.getFileTextPane();

        setInvoker(invokerTextPane);
        
        this.noCorrectionsItem = new JMenuItem("no corrections");
        this.noCorrectionsItem.setEnabled(false);
        initCommonItems();

    }

    public void show(MouseEvent mouseEvent) {
        if (mouseEvent == null) {
            LOGGER.error("mouseEvent is null");
            throw new NullPointerException("mouseEvent is null");
        }

        LOGGER.info("Popup menu triggered index at: " + cordsToCursor(mouseEvent));

        //removing all components
        removeAll();


        misspelledWord = spellCheckTab.getMisspelledWord(cordsToCursor(mouseEvent));

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

    private void addCorrectionsItems() {

        if (misspelledWord == null) {
            return;
        }

        List<String> corrections = spellCheckTab.getCorrections(misspelledWord);

        if (corrections.isEmpty()) {
            add(noCorrectionsItem);
            addToDictionoray();
            return;
        }


        for (String s : corrections) {
            add(new CorrectionItem(s));
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

    private void initCommonItems() {

        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setIcon(IconManager.getMenuIcon("cut.png"));
        cutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                invokerTextPane.cut();
            }
        });
        commonItems.add(cutItem);

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setIcon(IconManager.getMenuIcon("copy.png"));
        copyItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                invokerTextPane.copy();
            }
        });
        commonItems.add(copyItem);

        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setIcon(IconManager.getMenuIcon("paste.png"));
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
                    spellCheckTab.correct(correction, misspelledWord, cursorPosition);
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

                    spellCheckTab.addUserMisspelled(misspelled);
                    StatusManager.getInstance().setStatus(misspelled + " added to dictionary");
                }
            });
        }
    }
}
