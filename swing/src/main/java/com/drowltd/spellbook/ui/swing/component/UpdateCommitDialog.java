package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.exception.AuthenticationException;
import com.drowltd.spellbook.core.exception.UpdateServiceException;
import com.drowltd.spellbook.core.exception.UpdatesNotAvailableException;
import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.service.UpdateService;
import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.icons.JideIconsFactory;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javax.swing.JOptionPane.showConfirmDialog;

/**
 * @author ikkari
 *         Date: May 7, 2010
 *         Time: 4:24:27 PM
 */
public class UpdateCommitDialog extends StandardDialog implements UpdateService.ConflictHandler {

    private JProgressBar progressBar;
    private JButton updateButton;
    private JButton commitButton;
    private JButton cancelButton;
    private JList list;
    private JTabbedPane tabbedPane;
    private String acceptedText;

    private UpdateService updateService;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Translator TRANSLATOR = Translator.getTranslator("UpdateCommitDialog");
    private static Logger LOGGER = LoggerFactory.getLogger(UpdateCommitDialog.class);

    private static final int MIN_WIDTH = 350;
    private static final int MIN_HEIGHT = 400;
    private static final int FONT_SIZE = 11;

    public UpdateCommitDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        initComponents0();
    }

    private void initComponents0() {
        progressBar = new JProgressBar();
        commitButton = new JButton(TRANSLATOR.translate("UpdateCommitDialog(commitButton)"));
        cancelButton = new JButton(TRANSLATOR.translate("UpdateCommitDialog(cancelButton)"));
        updateButton = new JButton(TRANSLATOR.translate("UpdateCommitDialog(updateButton)"));
        tabbedPane = new JTabbedPane();

        list = new JList();
//        list.setBorder(new LineBorder(Color.BLACK, 1));
        tabbedPane.addTab(TRANSLATOR.translate("UpdateCommitDialog(modifiedWords)"), list);


        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progressBar.setIndeterminate(true);
                executor.execute(new UpdateTask());
            }
        });
        commitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!DictionaryService.getInstance().haveUncommited()) return;

                if (login()) {
                    progressBar.setIndeterminate(true);
                    executor.execute(new CommitTask());
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executor.shutdownNow();
                UpdateCommitDialog.this.dispose();
            }
        });

        setTitle(TRANSLATOR.translate("UpdateCommitDialog(Title)"));

        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(getParent());
    }

     @Override
    public JComponent createBannerPanel() {
        BannerPanel bannerPanel = new BannerPanel(TRANSLATOR.translate("BannerTitle(Message)"),
                TRANSLATOR.translate("Banner(Message)"),
                JideIconsFactory.getImageIcon("/icons/48x48/pencil.png"));
        bannerPanel.setFont(new Font("Tahoma", Font.PLAIN, FONT_SIZE));
        bannerPanel.setBackground(Color.WHITE);
        return bannerPanel;
    }

    @Override
    public JComponent createContentPanel() {

        JPanel panel = new JPanel(new MigLayout("wrap 1", "[grow]", "5[grow]5[]10"));
//        panel.add(message);
        panel.add(tabbedPane, "grow");
        panel.add(progressBar, "growx");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        buttonPanel.add(updateButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.add(commitButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.add(cancelButton, ButtonPanel.CANCEL_BUTTON);

        return buttonPanel;  
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
                JOptionPane.showMessageDialog(UpdateCommitDialog.this, TRANSLATOR.translate("UpdateCommitDialog(connectionFailure)"), "Info", JOptionPane.ERROR_MESSAGE);
                tryToConnect = false;

            } catch (AuthenticationException e) {
                LOGGER.warn(e.getMessage());

            } catch (SQLException e) {
                LOGGER.warn(e.getMessage());
                LOGGER.warn(String.valueOf(e.getErrorCode()));

                if (e.getErrorCode() == 0) {
                    JOptionPane.showMessageDialog(UpdateCommitDialog.this, TRANSLATOR.translate("UpdateCommitDialog(connectionFailure)"), "Info", JOptionPane.ERROR_MESSAGE);
                    tryToConnect = false;
                } else {
                    JOptionPane.showMessageDialog(UpdateCommitDialog.this, TRANSLATOR.translate("LoginDialog(loginIncorrect)"), "Info", JOptionPane.ERROR_MESSAGE);
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

    public void showDialog() {
        if (DictionaryService.getInstance().haveUncommited()) {
            list.setListData(DictionaryService.getInstance().getUncommitted().getDictionaryEntries().toArray());
        }

        pack();
        setVisible(true);
    }

    private class CommitTask implements Runnable {

        @Override
        public void run() {

            boolean isSuccessful = true;
            try {
                if (updateService.isOpen())
                    updateService.commit();
                else
                    showMessage(TRANSLATOR.translate("UpdateCommitDialog(commitFailedMessage)"), JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {

                updateService.close();
                isSuccessful = false;
            }

            if (isSuccessful)
                showMessage(TRANSLATOR.translate("UpdateCommitDialog(commitSuccesMessage)"), JOptionPane.INFORMATION_MESSAGE);
            else
                showMessage(TRANSLATOR.translate("UpdateCommitDialog(commitFailedMessage)"), JOptionPane.ERROR_MESSAGE);

            progressBar.setIndeterminate(false);
        }

        private void showMessage(final String message, final int type) {
            assert message != null && !message.isEmpty() : "message != null && !message.isEmpty()";

            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(UpdateCommitDialog.this, message, "Info", type);
                }
            });
        }
    }

    private class UpdateTask implements Runnable {

        @Override
        public void run() {
            final UpdateService us;
            UpdateService us0 = null;
            try {
                us = UpdateService.getInstance();
                us0 = us;
                boolean updateSuccessfull = true;
                try {
                    us.setHandler(UpdateCommitDialog.this);
                    us.update();
                } catch (UpdatesNotAvailableException ex) {
                    updateSuccessfull = false;
                    EventQueue.invokeLater(new UpdateResponseTask("No updates available"));
                }
                catch (InterruptedException ex) {
                    updateSuccessfull = false;
                    Thread.currentThread().interrupt();
                    EventQueue.invokeLater(new UpdateResponseTask(TRANSLATOR.translate("MessageDialog(Cancelled)")));
                }

                if (updateSuccessfull)
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JList uList = new JList();
                            uList.setListData(us.getUpdatedEntries().toArray());
                            tabbedPane.addTab(TRANSLATOR.translate("UpdateCommitDialog(updatedWords)"), uList);
                            tabbedPane.setSelectedIndex(1);
                            showMessage(TRANSLATOR.translate("UpdateCommitDialog(updateSuccesMessage)"));
                        }

                    });
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
                ex.printStackTrace(System.out);
                EventQueue.invokeLater(new UpdateResponseTask(TRANSLATOR.translate("MessageDialog(Error)")));
            }
            finally {
                if (us0 != null)
                    us0.close();
            }
        }
    }

    private class UpdateResponseTask implements Runnable {

        private String message;

        public UpdateResponseTask(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            showMessage(message);
        }

    }

    private void showMessage(String message) {
        progressBar.setIndeterminate(false);
        JOptionPane.showMessageDialog(UpdateCommitDialog.this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public String handle(final String word, final String base, final String remote) throws InterruptedException {
        if (word == null || base == null || remote == null) {
            LOGGER.error("base == null || remote == null");
            throw new IllegalArgumentException("base == null || remote == null");
        }

        if (base.isEmpty()) {
            LOGGER.error("base is empty");
            throw new IllegalArgumentException("base is empty");
        }

        if (remote.isEmpty()) {
            LOGGER.error("remote is empty");
            throw new IllegalArgumentException("remote is empty");
        }

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                JOptionPane.showMessageDialog(UpdateCommitDialog.this, TRANSLATOR.translate("MessageDialog(Conflict)"), "Info", JOptionPane.INFORMATION_MESSAGE);
                setAcceptedText(new DiffDialog(UpdateCommitDialog.this, true).diff(word, base, remote).showDialog());
            }
        });

        synchronized (this) {
            while (acceptedText == null)
                wait();
        }

        return acceptedText;
    }

    private synchronized void setAcceptedText(String acceptedText) {
        assert acceptedText != null : "acceptedText == null";
        this.acceptedText = acceptedText;
        notifyAll();
    }


    private static String[] showUserLoginDialog(Component parentComponent) {

        JPanel jp = new JPanel();
        jp.setLayout(new MigLayout("wrap 2", "[][grow][]", "[][]"));

        JTextField username = new JTextField(10);
        JTextField password = new JPasswordField(10);
        jp.add(new JLabel(TRANSLATOR.translate("LoginDialog(Username)")));
        jp.add(username);
        jp.add(new JLabel(TRANSLATOR.translate("LoginDialog(Password)")));
        jp.add(password);
        JCheckBox checkBox = new JCheckBox(TRANSLATOR.translate("LoginDialog(SaveUserPassword)"));
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
