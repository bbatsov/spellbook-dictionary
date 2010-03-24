package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.util.SearchUtils;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutocompletingTextField extends JTextField {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutocompletingTextField.class);

    private static int MAX_VISIBLE_COMPLETIONS = 5;

    private Completer completer;
    private JList completionList;
    private DefaultListModel completionListModel;
    private JScrollPane listScroller;
    private JWindow listWindow;
    private JList wordsList;
    private Window owner;

    public AutocompletingTextField() {
        completer = new Completer();

        completionListModel = new DefaultListModel();
        completionList = new JList(completionListModel);
        completionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        completionList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                LOGGER.info("Mouse pressed in completion list");

                listWindow.setVisible(false);

                final String completionString = (String) completionList.getSelectedValue();

                if (completionString != null && !completionString.isEmpty()) {
                    setText(completionString);
                    if (wordsList != null) {
                        wordsList.setSelectedValue(completionString, true);
                    }
                }
            }
        });

        listScroller = new JScrollPane(completionList,
                                       ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        listWindow = new JWindow(owner);
        listWindow.getContentPane().add(listScroller);
        listWindow.setFocusable(true);

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (KeyEvent.VK_ESCAPE == e.getKeyCode()) {
                    LOGGER.info("Escape pressed in word search field");

                    listWindow.setVisible(false);
                }

                if (KeyEvent.VK_DOWN == e.getKeyCode()) {
                    LOGGER.info("Down arrow pressed in word search field");

                    if (completionList.isSelectionEmpty()) {
                        completionList.setSelectedIndex(0);
                    } else {
                        completionList.setSelectedIndex(completionList.getSelectedIndex() + 1);
                    }
                }

                if (KeyEvent.VK_UP == e.getKeyCode()) {
                    LOGGER.info("Up arrow pressed in word search field");

                    if (!completionList.isSelectionEmpty()) {
                        completionList.setSelectedIndex(completionList.getSelectedIndex() - 1);
                    }
                }

                if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                    LOGGER.info("Enter pressed in word search field");

                    if (!completionList.isSelectionEmpty()) {
                        setText((String)completionList.getSelectedValue());
                    }
                }
            }
        });

        addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                hideCompletions();
            }

        });
    }

    public void setOwner(Window owner) {
        this.owner = owner;
    }

    public void setCompletions(List<String> completions) {
        completer.setCompletions(completions);
    }

    public void addCompletion(String s) {
        completer.addCompletion(s);
    }

    public void removeCompletion(String s) {
        completer.removeCompletion(s);
    }

    public void clearCompletions(String s) {
        completer.clearCompletions();
    }

    public void setWordsList(JList wordsList) {
        this.wordsList = wordsList;
    }

    public void showCompletions() {
        completer.buildAndShowPopup();
    }

    public void hideCompletions() {
        listWindow.setVisible(false);
    }

    /** inner class does the matching of the JTextField's
    document to completion strings kept in an ArrayList
     */
    class Completer implements DocumentListener {

        private List<String> completions;

        public Completer() {
            completions = new ArrayList();
            getDocument().addDocumentListener(this);
        }

        public void setCompletions(List<String> completionsList) {
            completions = completionsList;
        }

        public void addCompletion(String s) {
            if (!completions.contains(s)) {
                completions.add(s);
            }
        }

        public void removeCompletion(String s) {
            completions.remove(s);
            buildAndShowPopup();
        }

        public void clearCompletions() {
            completions.clear();
            buildPopup();
            listWindow.setVisible(false);
        }

        private void buildPopup() {
            completionListModel.clear();
            System.out.println("buildPopup for " + completions.size() + " completions");

            int startIndex = SearchUtils.findInsertionIndex(completions, getText()) + 1;
            System.out.println("Start index for " + getText() + " is " + startIndex);

            for (int i = startIndex; i < completions.size(); i++) {
                if (completions.get(i).toLowerCase().startsWith(getText().toLowerCase()) && !completions.get(i).toLowerCase().equals(getText().toLowerCase())) {
                    // add if match
                    System.out.println("matched " + completions.get(i));
                    completionListModel.add(completionListModel.getSize(), completions.get(i));
                } else {
                    System.out.println("pattern " + getText() + " does not match " + completions.get(i));
                    break;
                }
            }
        }

        private void showPopup() {
            if (completionListModel.getSize() == 0 || getText().isEmpty()) {
                listWindow.setVisible(false);
                return;
            }

            // figure out where the text field is,
            // and where its bottom left is
            java.awt.Point los = getLocationOnScreen();
            int popX = los.x;
            int popY = los.y + getHeight();
            listWindow.setLocation(popX, popY);
            // dynamically determine the size of the completion suggestion list
            listWindow.setSize(getSize().width, getSize().height * (completionListModel.getSize() < MAX_VISIBLE_COMPLETIONS ? completionListModel.getSize() : MAX_VISIBLE_COMPLETIONS));
            listWindow.setVisible(true);
        }

        private void buildAndShowPopup() {
            if (getText().length() < 1) {
                return;
            }

            buildPopup();
            showPopup();
        }

        // DocumentListener implementation
        @Override
        public void insertUpdate(DocumentEvent e) {
            // build popup only for interactive searches
            if (e.getLength() == 1) {
                buildAndShowPopup();
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            buildAndShowPopup();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            buildAndShowPopup();
        }
    }
}

