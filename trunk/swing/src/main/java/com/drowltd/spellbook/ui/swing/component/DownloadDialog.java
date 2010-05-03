package com.drowltd.spellbook.ui.swing.component;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.ButtonResources;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.plaf.UIDefaultsLookup;
import com.jidesoft.swing.FolderChooser;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class DownloadDialog extends StandardDialog implements PropertyChangeListener {
    private JButton downloadButton;
    private JButton changeFolderButton;
    private JTextField downloadUrlTextField;
    private JTextField downloadFolderTextField;
    private ProgressMonitor progressMonitor;
    private Task task;
    private String url;
    private String downloadFolder;
    private JButton okButton = new JButton();

    public DownloadDialog() {
        setModal(true);

        okButton.setEnabled(false);

        File currentDir = new File(".");

        downloadFolderTextField = new JTextField();
        downloadUrlTextField = new JTextField();
        downloadButton = new JButton("Download");
        changeFolderButton = new JButton("Change folder");

        try {
            downloadFolder = currentDir.getCanonicalPath();
            downloadFolderTextField.setText(downloadFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //careful not to overwrite existing files
                File file = new File(getDownloadPath());
                if (file.exists() &&
                        JOptionPane.showConfirmDialog(DownloadDialog.this,
                                "File already exists. Overwrite it?") != JOptionPane.YES_OPTION) {
                    System.out.println("do not download");
                } else {
                    task = new Task();
                    task.addPropertyChangeListener(DownloadDialog.this);
                    task.execute();
                    downloadButton.setEnabled(false);
                }
            }
        });

        changeFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FolderChooser folderChooser = new FolderChooser();

                int result = folderChooser.showSaveDialog(DownloadDialog.this);

                if (result == FolderChooser.APPROVE_OPTION) {
                    downloadFolder = folderChooser.getSelectedFolder().getAbsolutePath();
                    downloadFolderTextField.setText(downloadFolder);
                }
            }
        });

        setMinimumSize(new Dimension(600, 150));
    }

    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    public JComponent createContentPanel() {
        MigLayout layout = new MigLayout(
              "wrap 4",                 // Layout Constraints
              "[][][grow][]",   // Column constraints
              "[shrink 0][shrink 0]");    // Row constraints


        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        panel.add(new JLabel("Download url"));
        panel.add(downloadUrlTextField, "span 2, growx");
        panel.add(downloadButton, "growx");
        panel.add(new JLabel("Download folder"));
        panel.add(downloadFolderTextField, "span 2, growx");
        panel.add(changeFolderButton, "growx");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        JButton cancelButton = new JButton();
        JButton helpButton = new JButton();
        okButton.setName(OK);
        cancelButton.setName(CANCEL);
        helpButton.setName(HELP);
        buttonPanel.addButton(okButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(cancelButton, ButtonPanel.CANCEL_BUTTON);
        buttonPanel.addButton(helpButton, ButtonPanel.HELP_BUTTON);

        okButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.okButtonText")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_AFFIRMED);
                setVisible(false);
                dispose();
            }
        });
        cancelButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.cancelButtonText")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
                dispose();
            }
        });
        final ResourceBundle resourceBundle = ButtonResources.getResourceBundle(Locale.getDefault());
        helpButton.setAction(new AbstractAction(resourceBundle.getString("Button.help")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // do something
            }
        });
        helpButton.setMnemonic(resourceBundle.getString("Button.help.mnemonic").charAt(0));

        setDefaultCancelAction(cancelButton.getAction());
        setDefaultAction(okButton.getAction());
        getRootPane().setDefaultButton(okButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    public int showDialog(String url) {
        this.url = url;
        downloadUrlTextField.setText(url);
        progressMonitor = new ProgressMonitor(this, "Downloading url " + url, "Downloading", 0, 100);

        pack();

        setVisible(true);

        return getDialogResult();
    }

    public String getDownloadFolder() {
        return downloadFolder;
    }

    class Task extends SwingWorker<Void, Void> {
        private static final int BUFFER_SIZE = 1024;

        @Override
        public Void doInBackground() {
            try {
                URL dbUrl = new URL(url);
                setProgress(0);

                int contentLength = dbUrl.openConnection().getContentLength();

                BufferedInputStream in = new BufferedInputStream(dbUrl.openStream());
                FileOutputStream fos = new FileOutputStream(getDownloadPath());
                BufferedOutputStream bout = new BufferedOutputStream(fos, BUFFER_SIZE);
                byte[] data = new byte[BUFFER_SIZE];
                int x;
                int total = 0;

                System.out.println("Downloading file " + url);

                while ((x = in.read(data, 0, BUFFER_SIZE)) >= 0) {
                    total += x;
                    final int percents = (int) (((double) total / contentLength) * 100);
                    setProgress(percents);
                    bout.write(data, 0, x);
                }

                bout.close();
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            downloadButton.setEnabled(true);
        }

    }

    public String getDownloadPath() {
        return downloadFolder + File.separator + getFileName();
    }

    private String getFileName() {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            String message =
                    String.format("Completed %d%%.\n", progress);
            progressMonitor.setNote(message);
            if (progressMonitor.isCanceled() || task.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (progressMonitor.isCanceled()) {
                    task.cancel(true);
                    File file = new File(getDownloadPath());

                    if (file.exists()) {
                        // removing partially downloaded file
                        file.delete();
                    }

                    System.out.println("Task canceled.\n");
                } else {
                    okButton.setEnabled(true);

                    System.out.println("Task completed.\n");
                }

                downloadButton.setEnabled(true);
            }
        }

    }

    public static void main(String[] args) {
        DownloadDialog dialog = new DownloadDialog();

        dialog.showDialog("");

        System.exit(0);
    }
}
