package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.service.CodeHostingService;
import com.drowltd.spellbook.ui.swing.util.SwingUtil;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.icons.JideIconsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author ikkari
 *         Date: Jun 5, 2010
 *         Time: 7:09:03 PM
 */
public class IssueDialog extends BaseDialog {

    private static final int MIN_WIDTH = 350;
    private static final int MIN_HEIGHT = 400;
    private static final int BORDER_THICKNESS = 1;

    private JTextField titleTextField;
    private JTextArea contentTextArea;
    private JButton submitButton;
    private JButton cancelButton;
    private JLabel titleLabel;
    private JLabel contentLabel;

    public IssueDialog() {
        super(null, true);

        initComponents0();
    }

    private void initComponents0() {
        titleTextField = new JTextField();
        contentTextArea = new JTextArea();
        submitButton = new JButton(getTranslator().translate("Submit(Button)"));
        cancelButton = new JButton(getTranslator().translate("Cancel(Button)"));
        titleLabel = new JLabel(getTranslator().translate("IssueSummary(Title)"));
        contentLabel = new JLabel(getTranslator().translate("IssueDescription(Title)"));

        contentTextArea.setBorder(new LineBorder(Color.BLACK, BORDER_THICKNESS));

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
    }

    @Override
    public JComponent createBannerPanel() {
        return SwingUtil.createBannerPanel(getTranslator().translate("Banner(Title)"),
                getTranslator().translate("Banner(Message)"),
                JideIconsFactory.getImageIcon("/icons/48x48/bug-mail.png"));
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

        if (title.isEmpty()) {
            showMessage(getTranslator().translate("Message(Description)"));
            titleTextField.requestFocus();
            return;
        }

        if (content.isEmpty()) {
            showMessage(getTranslator().translate("Message(Summary)"));
            contentTextArea.requestFocus();
            return;
        }

        try {
            CodeHostingService.getInstance().createIssue(title, content);
        } catch (Exception e) {
            showMessage(getTranslator().translate("Message(Error)"));
        }

        setVisible(false);
    }

    private void cancelButtonActionPerformed() {
        setVisible(false);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, getTranslator().translate("Banner(Title)"), JOptionPane.INFORMATION_MESSAGE);
    }
}
