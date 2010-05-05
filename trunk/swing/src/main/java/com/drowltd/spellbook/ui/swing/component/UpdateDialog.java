package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.exception.UpdateServiceException;
import com.drowltd.spellbook.core.service.UpdateService;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author iivalchev
 */
public class UpdateDialog extends StandardDialog {

    private ExecutorService executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    private Future<?> updateFuture = null;
    private static Logger LOGGER = LoggerFactory.getLogger(UpdateDialog.class);
    private ResourceBundle bundle = ResourceBundle.getBundle("i18n/UpdateDialog");
    //components
    private JButton cancelButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton updateButton;

    /**
     * Creates new form UpdateDialog
     */
    public UpdateDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        setLocationRelativeTo(parent);
        initComponents0();
    }

    public void showUpdateDialog() {
        pack();
        checkForUpdates();
        progressBar.setIndeterminate(true);
        setVisible(true);
    }

    private void initComponents0() {
        progressBar = new JProgressBar();
        updateButton = new JButton();
        cancelButton = new JButton();
        statusLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/UpdateDialog");
        setTitle(bundle.getString("Dialog(Title)"));
        setResizable(false);

        updateButton.setText(bundle.getString("Dialog(UpdateButton)"));
        updateButton.setEnabled(false);

        cancelButton.setText(bundle.getString("Dialog(CancelButton)"));
        cancelButton.setMaximumSize(new Dimension(59, 29));
        cancelButton.setMinimumSize(new Dimension(59, 29));
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        statusLabel.setText(bundle.getString("Dialog(CheckingForUpdates)"));
        statusLabel.setVerticalAlignment(SwingConstants.TOP);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        if (updateFuture != null) {
            updateFuture.cancel(true);
            progressBar.setIndeterminate(false);
        }
        UpdateDialog.this.dispose();

    }

    public void setStatus(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        statusLabel.setText(message);


    }

    public void checkForUpdates() {
        updateFuture = executor.submit(new CheckForUpdatesTask());
        LOGGER.info("CheckForUpdates submitted");
    }

    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    public JComponent createContentPanel() {
        MigLayout migLayout = new MigLayout("wrap 1", "[][]");
        JPanel panel = new JPanel(migLayout);
        panel.add(statusLabel, "align 50 %");
        panel.add(progressBar, "growx");
        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        buttonPanel.add(updateButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.add(cancelButton, ButtonPanel.CANCEL_BUTTON);
        return buttonPanel;
    }

    private class CheckForUpdatesTask implements Runnable {

        @Override
        public void run() {
            UpdateService us;
            try {
                us = UpdateService.getInstance();
                LOGGER.info("checking for updates");
                if (us.checkForUpdates()) {
                    LOGGER.info("updates available");
                    EventQueue.invokeLater(new UpdateAvailableTask());
                } else {
                    LOGGER.info("updates NOT available");
                    EventQueue.invokeLater(new UpdateResponseTask(bundle.getString("MessageDialog(NoUpdates)")));
                }
            } catch (UpdateServiceException ex) {
                EventQueue.invokeLater(new UpdateResponseTask(bundle.getString("MessageDialog(NoConnection)")));
            }
        }
    }

    private class UpdateTask implements Runnable {

        @Override
        public void run() {
            UpdateService us = null;
            try {
                us = UpdateService.getInstance();
            } catch (UpdateServiceException ex) {
                EventQueue.invokeLater(new UpdateResponseTask(bundle.getString("MessageDialog(Error)")));
            }
            try {
                setStatus(bundle.getString("Dialog(Updating)"));
                us.update();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                EventQueue.invokeLater(new UpdateResponseTask(bundle.getString("MessageDialog(Cancelled)")));
            }

            EventQueue.invokeLater(new UpdateResponseTask(bundle.getString("MessageDialog(Success)")));
        }
    }

    private class UpdateAvailableTask implements Runnable {

        @Override
        public void run() {
            progressBar.setIndeterminate(false);
            setStatus("Updates available");
            updateButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    updateButton.setEnabled(false);
                    progressBar.setIndeterminate(true);
                    updateFuture = executor.submit(new UpdateTask());
                }
            });

            updateButton.setEnabled(true);
        }
    }

    private class UpdateResponseTask implements Runnable {

        private String message;

        public UpdateResponseTask(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            JOptionPane.showMessageDialog(UpdateDialog.this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
            UpdateDialog.this.dispose();
        }
    }
}
