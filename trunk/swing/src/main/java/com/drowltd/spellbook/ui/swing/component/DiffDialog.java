/*
 * DiffDialog.java
 *
 * Created on Apr 28, 2010, 10:38:12 AM
 */
package com.drowltd.spellbook.ui.swing.component;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ikkari
 */
public class DiffDialog extends StandardDialog {

    private static Logger LOGGER = LoggerFactory.getLogger(DiffDialog.class);

    private javax.swing.JButton jAcceptBaseButton;
    private javax.swing.JButton jAcceptRemoteButton;
    private javax.swing.JScrollPane jBaseScrollPane;
    private javax.swing.JTextPane jBaseTextPane;
    private javax.swing.JScrollPane jRemoteScrollPane;
    private javax.swing.JTextPane jRemoteTextPane;

    private ResourceBundle bundle = ResourceBundle.getBundle("i18n/DiffDialog");
    private String acceptedText = "";

    /**
     * Creates new form DiffDialog
     */
    public DiffDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        initComponents0();
    }

    public DiffDialog diff(String baseText, String remoteText) {
        if (baseText == null || remoteText == null) {
            throw new IllegalArgumentException("baseText == null || remoteText==null");
        }

        jBaseTextPane.setText(baseText);
        jRemoteTextPane.setText(remoteText);
        highlightPane(jBaseTextPane, jRemoteTextPane);

        return this;
    }


    private void initComponents0() {
        jBaseScrollPane = new javax.swing.JScrollPane();
        jBaseTextPane = new javax.swing.JTextPane();
        jRemoteScrollPane = new javax.swing.JScrollPane();
        jRemoteTextPane = new javax.swing.JTextPane();
        jAcceptBaseButton = new javax.swing.JButton();
        jAcceptRemoteButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jBaseTextPane.setBackground(new java.awt.Color(255, 255, 255));
        jBaseScrollPane.setViewportView(jBaseTextPane);

        jRemoteTextPane.setBackground(new java.awt.Color(255, 255, 255));
        jRemoteScrollPane.setViewportView(jRemoteTextPane);

        jAcceptBaseButton.setText(bundle.getString("Dialog(AcceptBaseButton)"));

        jAcceptRemoteButton.setText(bundle.getString("Dialog(AcceptRemoteButton)"));

        jBaseScrollPane.getViewport().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                jRemoteScrollPane.getViewport().setViewPosition(jBaseScrollPane.getViewport().getViewPosition());
            }
        });

        jRemoteScrollPane.getViewport().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                jBaseScrollPane.getViewport().setViewPosition(jRemoteScrollPane.getViewport().getViewPosition());
            }
        });

        jAcceptRemoteButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                acceptedText = jRemoteTextPane.getText();

                DiffDialog.this.setVisible(false);
                DiffDialog.this.dispose();
            }
        });

        jAcceptBaseButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                acceptedText = jBaseTextPane.getText();
                DiffDialog.this.setVisible(false);
                DiffDialog.this.dispose();
            }
        });

    }


    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    public JComponent createContentPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("wrap 2", "[grow][grow]", "[grow][]"));

        panel.add(jBaseScrollPane, "growx, growy, w 320, h 400");
        panel.add(jRemoteScrollPane, "growx, growy, w 320, h 400");
        panel.add(jAcceptBaseButton, "align 20%");
        panel.add(jAcceptRemoteButton, "align 20%");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        return null;
    }

    public String showDialog() {
        pack();

        setVisible(true);

        getDialogResult();
        return acceptedText;
    }


    List<StringLine> lcs(List<StringLine> base, List<StringLine> remote) {
        return new LCSFinder(base, remote).find();
    }

    private void highlightPane(JTextPane base, JTextPane remote) {
        assert base != null && remote != null;

        List<StringLine> clines = breakIntoLines(remote.getText());
        clines.removeAll(lcs(breakIntoLines(base.getText()), breakIntoLines(remote.getText())));

        DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN);

        for (StringLine line : clines) {
            try {
                remote.getHighlighter().addHighlight(line.offset, line.offset + line.len, painter);
            } catch (BadLocationException ex) {
                LOGGER.error(ex.getMessage());
            }
        }

    }

    List<StringLine> breakIntoLines(String text) {
        assert text != null && !text.isEmpty() : "text is null or empty";

        String[] lines = text.split("\r?\n");

        ArrayList<StringLine> list = new ArrayList<StringLine>();

        String pLine = lines[0];
        int offset = 0;
        list.add(new StringLine(pLine, 0, pLine.length()));

        for (int i = 1; i < lines.length; ++i) {

            LOGGER.info("adding StringLine: offset: " + (offset + pLine.length() + 1) + " lenght: " + lines[i].length());
            list.add(new StringLine(lines[i], offset + pLine.length() + 1, lines[i].length()));
            offset = offset + pLine.length() + 1;
            pLine = lines[i];

        }

        return list;
    }

    public String getAcceptedText() {
        return null;
    }

    static class StringLine {

        String line;
        int offset;
        int len;

        public StringLine(String line, int offset, int len) {
            this.line = line;
            this.offset = offset;
            this.len = len;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (StringLine.class != o.getClass()) {
                return false;
            }

            StringLine sl = (StringLine) o;
            if (len != sl.len) {
                return false;
            }

            if (line.equals(sl.line)) {
                return true;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + (this.line != null ? this.line.hashCode() : 0);
            hash = 11 * hash + this.len;
            return hash;
        }
    }

    static class LCSFinder {

        List<StringLine> base;
        List<StringLine> remote;
        Deque<Invocable> stack = new LinkedList<Invocable>();

        public LCSFinder(List<StringLine> base, List<StringLine> remote) {
            this.base = base;
            this.remote = remote;
        }

        public List<StringLine> find() {
            List<StringLine> result = new LinkedList<StringLine>();
            stack.push(new LCSTask(base.size(), remote.size(), result));
            Invocable task;
            while ((task = stack.poll()) != null) {
                task.invoke();
            }

            return result;
        }

        class MergeTask extends Invocable {

            MergeTask(int baseSize, int remoteSize, List<StringLine> result) {
                super(baseSize, remoteSize, result);
            }

            @Override
            public void invoke() {
                result.add(base.get(baseSize - 1));
            }
        }

        class SplitTask extends Invocable {

            List<StringLine> resultBase;
            List<StringLine> resultRemote;

            public SplitTask(int baseSize, int remoteSize, List<StringLine> result, List<StringLine> resultBase, List<StringLine> resultRemote) {
                super(baseSize, remoteSize, result);

                this.resultBase = resultBase;
                this.resultRemote = resultRemote;
            }

            @Override
            public void invoke() {
                result.addAll(resultBase.size() > resultRemote.size() ? resultBase : resultRemote);
            }
        }

        class LCSTask extends Invocable {

            LCSTask(int baseSize, int remoteSize, List<StringLine> result) {
                super(baseSize, remoteSize, result);
            }

            @Override
            public void invoke() {
                if (baseSize == 0 || remoteSize == 0) {
                    return;
                }

                if (base.get(baseSize - 1).equals(remote.get(remoteSize - 1))) {
                    stack.push(new MergeTask(baseSize, remoteSize, result));
                    stack.push(new LCSTask(baseSize - 1, remoteSize - 1, result));
                } else {
                    List<StringLine> resultBase = new LinkedList<StringLine>();
                    List<StringLine> resultRemote = new LinkedList<StringLine>();
                    stack.push(new SplitTask(baseSize, remoteSize, result, resultBase, resultRemote));
                    stack.push(new LCSTask(baseSize - 1, remoteSize, resultBase));
                    stack.push(new LCSTask(baseSize, remoteSize - 1, resultRemote));
                }
            }
        }

        abstract class Invocable {

            int baseSize;
            int remoteSize;
            List<StringLine> result;

            Invocable(int baseSize, int remoteSize, List<StringLine> result) {
                this.baseSize = baseSize;
                this.remoteSize = remoteSize;
                this.result = result;
            }

            abstract void invoke();
        }
    }
}
