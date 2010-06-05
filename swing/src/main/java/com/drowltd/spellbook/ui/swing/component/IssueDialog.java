package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.service.CodeHostingService;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author ikkari
 *         Date: Jun 5, 2010
 *         Time: 7:09:03 PM
 */
public class IssueDialog extends StandardDialog {

    private static final int MIN_WIDTH = 350;
    private static final int MIN_HEIGHT = 400;

    private JTextField titleTextField;
    private JTextArea contentTextArea;
    private JButton submitButton;
    private JButton cancelButton;
    private JLabel titleLabel;
    private JLabel contentLabel;

    public IssueDialog() {
        initComponents0();
    }

    private void initComponents0() {
        titleTextField = new JTextField();
        contentTextArea = new JTextArea();
        submitButton = new JButton("submit");
        cancelButton = new JButton("cancel");
        titleLabel = new JLabel("Issue Title");
        contentLabel = new JLabel("Content");

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitButtonActionPerformed();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed();
            }
        });

        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(getParent());
    }

    @Override
    public JComponent createBannerPanel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1", "[grow]", "[][][][grow]"));

        panel.add(titleLabel, "align left");
        panel.add(titleTextField, "growx");
        panel.add(contentLabel, "align left");
        panel.add(contentTextArea, "grow");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        buttonPanel.add(submitButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.add(cancelButton, ButtonPanel.CANCEL_BUTTON);
        return buttonPanel;
    }

    private void submitButtonActionPerformed() {
        String title = titleTextField.getText();
        String content = contentTextArea.getText();

        if (!(title.isEmpty() || content.isEmpty())) {
            //perhaps, some better handling
            try {
                CodeHostingService.getInstance().createIssue(title, content);
            } catch (Exception e) {
                showMessage("can't submit issue");
            }
        }

        this.dispose();
    }

    private void cancelButtonActionPerformed() {
        this.dispose();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(IssueDialog.this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showDialog() {
        pack();
        setVisible(true);
    }
}
