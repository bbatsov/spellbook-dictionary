package com.drowltd.dictionary.ui.desktop;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class CompletableJTextField extends JTextField {

    private Completer completer;
    private JList completionList;
    private DefaultListModel completionListModel;
    private JScrollPane listScroller;
    private JWindow listWindow;
    private JList wordsList;

    public CompletableJTextField() {
        completer = new Completer();

        completionListModel = new DefaultListModel();
        completionList = new JList(completionListModel);
        completionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        completionList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                listWindow.setVisible(false);

                final String completionString = (String) completionList.getSelectedValue();

                Thread worker = new Thread() {

                    @Override
                    public void run() {
                        if (completionString != null && !completionString.isEmpty()) {
                            setText(completionString);
                            wordsList.setSelectedValue(completionString, true);
                        }
                    }
                };

                SwingUtilities.invokeLater(worker);
            }
        });

        listScroller = new JScrollPane(completionList,
                                       ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        listWindow = new JWindow();
        listWindow.getContentPane().add(listScroller);

        addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                listWindow.setVisible(false);
            }
        });
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

    /** inner class does the matching of the JTextField's
    document to completion strings kept in an ArrayList
     */
    class Completer implements DocumentListener {

        private List<String> completions;

        public Completer() {
            completions = new ArrayList();
            getDocument().addDocumentListener(this);
        }

        public void addCompletion(String s) {
            completions.add(s);
            //buildAndShowPopup();
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

            for (String completion : completions) {
                if (completion.startsWith(getText())) {
                    // add if match
                    System.out.println("matched " + completion);
                    completionListModel.add(completionListModel.getSize(), completion);
                } else {
                    System.out.println("pattern " + getText() + " does not match " + completion);
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
            listWindow.setSize(getSize().width, getSize().height * 5);
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
            buildAndShowPopup();
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

