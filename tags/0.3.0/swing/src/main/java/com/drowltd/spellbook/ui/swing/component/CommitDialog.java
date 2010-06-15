package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.exception.AuthenticationException;
import com.drowltd.spellbook.core.exception.UpdateServiceException;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.service.UpdateService;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javax.swing.JOptionPane.showConfirmDialog;

/**
 * @author ikkari
 *         Date: May 7, 2010
 *         Time: 4:24:27 PM
 */
public class CommitDialog extends StandardDialog {

    private JLabel message;
    private JProgressBar progressBar;
    private JButton commitButton;
    private JButton cancelButton;
    private JList list;

    private UpdateService updateService;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private static ResourceBundle bundle = ResourceBundle.getBundle("i18n/CommitDialog");
    private static Logger LOGGER = LoggerFactory.getLogger(CommitDialog.class);

    public CommitDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        setLocationRelativeTo(parent);
        initComponents0();
    }

    private void initComponents0() {
        message = new JLabel(bundle.getString("CommitDialog(Message)"));
        progressBar = new JProgressBar();
        commitButton = new JButton(bundle.getString("CommitDialog(commitButton)"));
        cancelButton = new JButton(bundle.getString("CommitDialog(cancelButton)"));
        list = new JList();
                     list.setBorder(new LineBorder(Color.BLACK, 1));

        commitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progressBar.setIndeterminate(true);
                message.setText(bundle.getString("CommitDialog(commitMessage)"));
                executor.execute(new CommitTask());
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executor.shutdown();
                CommitDialog.this.dispose();
            }
        });

        setTitle(bundle.getString("CommitDialog(Title)"));
    }
    

    @Override
    public JComponent createBannerPanel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JComponent createContentPanel() {

        JPanel panel = new JPanel(new MigLayout("wrap 1", "[][grow][grow]", "5[]5[]5[]10"));
        panel.add(message);
        panel.add(list,"grow, h 250, w 200");
        panel.add(progressBar,"grow");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        buttonPanel.add(commitButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.add(cancelButton, ButtonPanel.CANCEL_BUTTON);

        return buttonPanel;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private boolean login() {
        String username = PreferencesManager.getInstance().get(PreferencesManager.Preference.REMOTE_DB_USERNAME, null);
        String password = PreferencesManager.getInstance().get(PreferencesManager.Preference.REMOTE_DB_PASSWORD, null);

        boolean tryToConnect = true;
        while (tryToConnect) {
            try {
                updateService = UpdateService.getInstance(username, password);
                return true;
            } catch (UpdateServiceException e) {
                JOptionPane.showMessageDialog(CommitDialog.this, bundle.getString("CommitDialog(connectionFailure)"), "Info", JOptionPane.ERROR_MESSAGE);
                tryToConnect = false;

            } catch (AuthenticationException e) {
                LOGGER.warn(e.getMessage());

            } catch (SQLException e) {
                LOGGER.warn(e.getMessage());
                LOGGER.warn(String.valueOf(e.getErrorCode()));

                if (e.getErrorCode() == 0) {
                    JOptionPane.showMessageDialog(CommitDialog.this, bundle.getString("CommitDialog(connectionFailure)"), "Info", JOptionPane.ERROR_MESSAGE);
                    tryToConnect = false;
                } else {
                    JOptionPane.showMessageDialog(CommitDialog.this, bundle.getString("LoginDialog(loginIncorrect)"), "Info", JOptionPane.ERROR_MESSAGE);
                }
            }

            if (tryToConnect) {
                String[] loginInfo = showUserLoginDialog(this);
                if (loginInfo[2].equals("close"))
                    tryToConnect = false;
                else {
                    username = loginInfo[0];
                    password = loginInfo[1];
                }
            }
        }
        return false;
    }

    public void showUpdateDialog() {
        if (login()) {
            if (!updateService.haveUncommited()) {
                JOptionPane.showMessageDialog(CommitDialog.this, bundle.getString("CommitDialog(noUncommitted)"), "Info", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                return;
            }
            list.setListData(updateService.getUncommitted().getDictionaryEntries().toArray());
            pack();
            setVisible(true);
        }
    }

    private class CommitTask implements Runnable {

        @Override
        public void run() {

            boolean isSuccessful = true;
            try {
                if (updateService.isOpen())
                    updateService.commit();
                else
                    showMessasge(bundle.getString("CommitDialog(commitFailedMessage)"), JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                
                updateService.close();
                isSuccessful = false;
            }

            if (isSuccessful)
                showMessasge(bundle.getString("CommitDialog(commitSuccesMessage)"), JOptionPane.INFORMATION_MESSAGE);
            else
                showMessasge(bundle.getString("CommitDialog(commitFailedMessage)"), JOptionPane.ERROR_MESSAGE);

        }

        private void showMessasge(final String message, final int type) {
            assert message != null && !message.isEmpty() : "message != null && !message.isEmpty()";

            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(CommitDialog.this, message, "Info", type);
                    CommitDialog.this.dispose();
                }
            });
        }
    }

    private static String[] showUserLoginDialog(Component parentComponent) {

        JPanel jp = new JPanel();
        jp.setLayout(new MigLayout("wrap 2", "[][grow][]", "[][]"));

        JTextField username = new JTextField(10);
        JTextField password = new JPasswordField(10);
        jp.add(new JLabel(bundle.getString("LoginDialog(Username)")));
        jp.add(username);
        jp.add(new JLabel(bundle.getString("LoginDialog(Password)")));
        jp.add(password);
        JCheckBox checkBox = new JCheckBox(bundle.getString("LoginDialog(SaveUserPassword)"));
        jp.add(checkBox, "align left, span 2");
        int n = showConfirmDialog(parentComponent, jp, "", JOptionPane.OK_CANCEL_OPTION);
        if (n == JOptionPane.OK_OPTION) {
            if (checkBox.isSelected()) {
                PreferencesManager.getInstance().put(PreferencesManager.Preference.REMOTE_DB_USERNAME, username.getText());
                PreferencesManager.getInstance().put(PreferencesManager.Preference.REMOTE_DB_PASSWORD, password.getText());
            }
            return new String[]{username.getText(), password.getText(), ""};
        } else {
            return new String[]{"", "", "close"};
        }
    }
}