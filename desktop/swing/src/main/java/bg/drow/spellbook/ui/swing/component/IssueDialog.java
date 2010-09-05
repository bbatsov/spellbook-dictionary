package bg.drow.spellbook.ui.swing.component;

import bg.drow.spellbook.core.service.FeedbackService;
import com.jidesoft.dialog.ButtonPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 */
public class IssueDialog extends BaseDialog {
    private static final int MIN_WIDTH = 350;
    private static final int MIN_HEIGHT = 400;

    private static final int BORDER_THICKNESS = 1;

    private JTextField titleTextField;
    private JTextArea contentTextArea;
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JButton submitButton;
    private JButton cancelButton;

    public IssueDialog() {
        super((Frame) null, true);

        titleTextField = new JTextField();
        contentTextArea = new JTextArea();
        usernameTextField = new JTextField();
        passwordTextField = new JPasswordField();
        submitButton = new JButton(getTranslator().translate("Submit(Button)"));
        cancelButton = new JButton(getTranslator().translate("Cancel(Button)"));

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
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1", "[grow]", "[][][][grow][][]"));

        panel.add(new JLabel(getTranslator().translate("IssueSummary(Title)")), "align left");
        panel.add(titleTextField, "growx");
        panel.add(new JLabel(getTranslator().translate("IssueDescription(Title)")), "align left");
        panel.add(contentTextArea, "grow");
        panel.add(new JLabel(getTranslator().translate("Username(Label)")), "split 3, align right");
        panel.add(usernameTextField, "growx");
        panel.add(new JLabel("@gmail.com"));
        panel.add(new JLabel(getTranslator().translate("Password(Label)")), "split 2, align right");
        panel.add(passwordTextField, "growx");

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
            FeedbackService feedbackService = new FeedbackService(usernameTextField.getText(), String.valueOf(passwordTextField.getPassword()));

            feedbackService.createIssue(title, content);
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
