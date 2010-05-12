package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.ButtonResources;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.UIDefaultsLookup;
import com.jidesoft.swing.FolderChooser;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javax.swing.border.BevelBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadDialog.class);
    private static final Translator TRANSLATOR = Translator.getTranslator("DownloadDialog");

    private JButton downloadButton;
    private JButton changeFolderButton;
    private JTextField downloadUrlTextField;
    private JTextField localDbFolderTextField;
    private ProgressMonitor progressMonitor;
    private Task task;
    private String localDbFolder;
    private JButton okButton = new JButton();
    private static final int MIN_WIDTH = 600;
    private static final int MIN_HEIGHT = 270;

    private static final String DB_URL = "http://spellbook-dictionary.googlecode.com/files/spellbook-db-0.3.tar.bz2";
    private static final String DOWNLOAD_DIR = System.getProperty("java.io.tmpdir");
    private static final int FONT_SIZE = 14;

    public DownloadDialog() {
        setModal(true);

        localDbFolderTextField = new JTextField();
        localDbFolderTextField.setEditable(false);
        downloadUrlTextField = new JTextField(DB_URL);
        downloadUrlTextField.setEditable(false);
        downloadButton = new JButton(TRANSLATOR.translate("Download(Button)"), IconManager.getImageIcon("data_down.png", IconManager.IconSize.SIZE24));
        changeFolderButton = new JButton(TRANSLATOR.translate("ChangeFolder(Button)"), IconManager.getImageIcon("data_find.png", IconManager.IconSize.SIZE24));
        progressMonitor = new ProgressMonitor(this, "Downloading url " + DB_URL, "Downloading", 0, 100);

        localDbFolder = System.getProperty("user.home");
        localDbFolderTextField.setText(localDbFolder);

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //careful not to overwrite existing files
                File file = new File(getDownloadedDbPath());
                if (file.exists() &&
                        JOptionPane.showConfirmDialog(DownloadDialog.this,
                                TRANSLATOR.translate("Overwrite(Message)")) != JOptionPane.YES_OPTION) {
                    LOGGER.info("don't overwrite existing file");
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

                int result = folderChooser.showOpenDialog(DownloadDialog.this);

                if (result == FolderChooser.APPROVE_OPTION) {
                    localDbFolder = folderChooser.getSelectedFolder().getAbsolutePath();
                    localDbFolderTextField.setText(localDbFolder);

                    File dbFile = new File(localDbFolder + File.separator + getFileName());

                    if (dbFile.exists()) {
                        okButton.getAction().setEnabled(true);
                    }
                }
            }
        });

        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(null);
        setTitle(TRANSLATOR.translate("SelectDb(Title)"));
    }

    @Override
    public JComponent createBannerPanel() {
        BannerPanel bannerPanel = new BannerPanel(TRANSLATOR.translate("MissingDb(Title)"), TRANSLATOR.translate("MissingDb(Message)"),
                JideIconsFactory.getImageIcon("/icons/48x48/data_unknown.png"));
        bannerPanel.setFont(new Font("Tahoma", Font.PLAIN, FONT_SIZE));
        bannerPanel.setBackground(Color.WHITE);
        bannerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        return bannerPanel;
    }

    @Override
    public JComponent createContentPanel() {
        MigLayout layout = new MigLayout(
                "wrap 4",                 // Layout Constraints
                "[][][grow][]",   // Column constraints
                "[shrink 0][shrink 0]");    // Row constraints


        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        panel.add(new JLabel(TRANSLATOR.translate("DownloadUrl(Label)")));
        panel.add(downloadUrlTextField, "span 2, growx");
        panel.add(downloadButton, "growx");
        panel.add(new JLabel(TRANSLATOR.translate("DbFolder(Label)")));
        panel.add(localDbFolderTextField, "span 2, growx");
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

        okButton.getAction().setEnabled(false);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    public int showDialog() {
        pack();

        setVisible(true);

        return getDialogResult();
    }

    class Task extends SwingWorker<Void, Void> {
        private static final int BUFFER_SIZE = 1024;

        @Override
        public Void doInBackground() {
            try {
                URL dbUrl = new URL(DB_URL);
                setProgress(0);

                int contentLength = dbUrl.openConnection().getContentLength();

                BufferedInputStream in = new BufferedInputStream(dbUrl.openStream());
                FileOutputStream fos = new FileOutputStream(getDownloadedDbPath());
                BufferedOutputStream bout = new BufferedOutputStream(fos, BUFFER_SIZE);
                byte[] data = new byte[BUFFER_SIZE];
                int x;
                int total = 0;

                LOGGER.info("Downloading file " + DB_URL);

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

    public String getDownloadedDbPath() {
        return DOWNLOAD_DIR + File.separator + getFileName();
    }

    private String getFileName() {
        return DB_URL.substring(DB_URL.lastIndexOf("/") + 1);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            String message = String.format("Completed %d%%.\n", progress);
            progressMonitor.setNote(message);
            if (progressMonitor.isCanceled() || task.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (progressMonitor.isCanceled()) {
                    task.cancel(true);
                    File file = new File(getDownloadedDbPath());

                    if (file.exists()) {
                        // removing partially downloaded file
                        if (file.delete()) {
                            LOGGER.info("partial download successfully deleted");
                        } else {
                            LOGGER.info("failed to delete partially downloaded file " + file.getAbsolutePath());
                        }
                    }

                    LOGGER.info("Task canceled.\n");
                } else {
                    okButton.getAction().setEnabled(true);

                    LOGGER.info("Task completed.\n");
                }

                downloadButton.setEnabled(true);
            }
        }

    }

    public static void main(String[] args) {
        DownloadDialog dialog = new DownloadDialog();

        dialog.showDialog();

        System.exit(0);
    }
}
