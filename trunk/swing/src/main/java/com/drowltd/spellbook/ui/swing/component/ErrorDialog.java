package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.i18n.Translator;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialogPane;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorDialog extends BaseDialog {
    private static final Translator TRANSLATOR = Translator.getTranslator("ErrorDialog");

    private JComponent detailsPanel;
    private Throwable throwable;

    public ErrorDialog(Frame parent, Throwable throwable) throws HeadlessException {
        super(parent, true);

        setTitle("ErrorDialog(Title)");

        this.throwable = throwable;
    }

    public JComponent createDetailsPanel() {
        JTextArea textArea = new JTextArea(getStackTraceAsString(throwable));
        textArea.setRows(10);

        JLabel label = new JLabel(TRANSLATOR.translate("Details(Label)") + ": ");

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(textArea));
        panel.add(label, BorderLayout.BEFORE_FIRST_LINE);
        label.setLabelFor(textArea);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        return panel;
    }

    @Override
    protected StandardDialogPane createStandardDialogPane() {
        return new DefaultStandardDialogPane() {
            @Override
            protected void layoutComponents(Component bannerPanel, Component contentPanel, ButtonPanel buttonPanel) {
                setLayout(new JideBoxLayout(this, BoxLayout.Y_AXIS));
                if (bannerPanel != null) {
                    add(bannerPanel);
                }
                if (contentPanel != null) {
                    add(contentPanel);
                }
                add(buttonPanel, JideBoxLayout.FIX);
                detailsPanel = createDetailsPanel();
                add(detailsPanel, JideBoxLayout.VARY);
                detailsPanel.setVisible(false);
            }
        };
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        JLabel label = new JLabel(TRANSLATOR.translate("UnexpectedError(Label)") + ": " + (throwable.getLocalizedMessage() != null ?
                throwable.getLocalizedMessage() : throwable.getClass().toString()));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        JButton closeButton = new JButton();
        JButton detailButton = new JButton();
        detailButton.setMnemonic('D');
        closeButton.setName(OK);
        buttonPanel.addButton(closeButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(detailButton, ButtonPanel.OTHER_BUTTON);

        closeButton.setAction(new AbstractAction(TRANSLATOR.translate("Close(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_AFFIRMED);
                setVisible(false);
                dispose();
            }
        });

        detailButton.setAction(new AbstractAction(TRANSLATOR.translate("Details(Label)") + " >>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (detailsPanel.isVisible()) {
                    detailsPanel.setVisible(false);
                    putValue(Action.NAME, TRANSLATOR.translate("Details(Label)") + " <<");
                    pack();
                } else {
                    detailsPanel.setVisible(true);
                    putValue(Action.NAME, TRANSLATOR.translate("Details(Label)") + " Details");
                    pack();
                }
            }
        });

        setDefaultCancelAction(closeButton.getAction());
        setDefaultAction(closeButton.getAction());
        getRootPane().setDefaultButton(closeButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setSizeConstraint(ButtonPanel.NO_LESS_THAN); // since the checkbox is quite wide, we don't want all of them have the same size.
        return buttonPanel;
    }

    private String getStackTraceAsString(final Throwable pThrowable) {
        final StringWriter tStringWriter = new StringWriter();
        final PrintWriter tPrintWriter = new PrintWriter(tStringWriter);

        //capture the stack trace in the writer
        pThrowable.printStackTrace(tPrintWriter);

        return tStringWriter.toString();
    }

}

