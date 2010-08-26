package bg.drow.spellbook.ui.desktop;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.Language;
import bg.drow.spellbook.core.model.SupportedFileType;
import bg.drow.spellbook.core.service.DictionaryServiceImpl;
import bg.drow.spellbook.ui.swing.component.LanguageComboBox;
import bg.drow.spellbook.core.model.DictionaryEntry;
import bg.drow.spellbook.ui.swing.component.BaseDialog;
import com.jidesoft.dialog.ButtonPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Import dictionary dialog.
 *
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class ImportDialog extends BaseDialog {
    private LanguageComboBox fromComboBox;
    private LanguageComboBox toComboBox;
    private static final int BUFFER_SIZE = 20000;
    private JButton importButton = new JButton();
    private DictionaryImporter importer = new BgOfficeImporter();
    private SupportedFileType selectedFileType = SupportedFileType.BGOFFICE;

    public ImportDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
        DictionaryServiceImpl.init();
    }

    @Override
    public JComponent createContentPanel() {
        JPanel mainPanel = new JPanel(new MigLayout("wrap 3", "[grow][grow][grow]", "[][][]"));

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryFile(Label)")), "grow");

        final JTextField dictionaryFileTextField = new JTextField();
        dictionaryFileTextField.setEditable(false);

        mainPanel.add(dictionaryFileTextField, "growx, width 200::");

        JButton selectDictionaryFileButton = new JButton(getTranslator().translate("SelectFile(Button)"));

        selectDictionaryFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();

                if (fileChooser.showOpenDialog(ImportDialog.this) == JFileChooser.APPROVE_OPTION) {
                    dictionaryFileTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        mainPanel.add(selectDictionaryFileButton, "growx");

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryFormat(Label)")), "growx");

        JComboBox fileFormatComboBox = new JComboBox();

        fileFormatComboBox.setModel(new DefaultComboBoxModel(SupportedFileType.values()));

        fileFormatComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                attachImporter((SupportedFileType) e.getItem());
            }
        });

        mainPanel.add(fileFormatComboBox, "span2, growx");

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryType(Label)")), "growx");

        ButtonGroup buttonGroup = new ButtonGroup();

        JRadioButton normalRadioButton = new JRadioButton(getTranslator().translate("Normal(RadioButton)"));

        normalRadioButton.setSelected(true);

        buttonGroup.add(normalRadioButton);

        mainPanel.add(normalRadioButton);

        final JRadioButton specialRadioButton = new JRadioButton(getTranslator().translate("Special(RadioButton)"));

        buttonGroup.add(specialRadioButton);

        mainPanel.add(specialRadioButton);

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryFromLang(Label)")), "grow");

        fromComboBox = new LanguageComboBox();

        mainPanel.add(fromComboBox, "span 2, growx");

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryToLang(Label)")));

        toComboBox = new LanguageComboBox();

        mainPanel.add(toComboBox, "span2, growx");

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryName(Label)")), "growx");

        final JTextField dictionaryNameTextField = new JTextField();

        mainPanel.add(dictionaryNameTextField, "span 2, growx, width 200::");

        mainPanel.add(new JLabel(getTranslator().translate("DictionarySmallIcon(Label)")), "growx");

        final JTextField dictionaryIconTextField = new JTextField();

        dictionaryIconTextField.setEditable(false);

        mainPanel.add(dictionaryIconTextField, "growx, width 200::");

        JButton selectIconButton = new JButton(getTranslator().translate("SelectSmallIcon(Button)"));

        selectIconButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();

                if (fileChooser.showOpenDialog(ImportDialog.this) == JFileChooser.APPROVE_OPTION) {
                    dictionaryIconTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        mainPanel.add(selectIconButton, "growx");

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryBigIcon(Label)")), "growx");

        final JTextField dictionaryBigIconTextField = new JTextField();

        dictionaryBigIconTextField.setEditable(false);

        mainPanel.add(dictionaryBigIconTextField, "growx, width 200::");

        JButton selectBigIconButton = new JButton(getTranslator().translate("SelectBigIcon(Button)"));

        selectBigIconButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();

                if (fileChooser.showOpenDialog(ImportDialog.this) == JFileChooser.APPROVE_OPTION) {
                    dictionaryBigIconTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        mainPanel.add(selectBigIconButton, "growx");

        importButton.setAction(new AbstractAction(getTranslator().translate("Import(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                importer.importDictionary((Language) fromComboBox.getSelectedItem(), (Language) toComboBox.getSelectedItem(),
                        dictionaryNameTextField.getText(), specialRadioButton.isSelected(),
                        dictionaryIconTextField.getText(), dictionaryBigIconTextField.getText(), dictionaryFileTextField.getText());
            }
        });

        return mainPanel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel(SwingConstants.RIGHT);

        JButton closeButton = createCloseButton();

        buttonPanel.addButton(importButton);
        buttonPanel.addButton(closeButton);

        setDefaultCancelAction(closeButton.getAction());
        setDefaultAction(importButton.getAction());
        getRootPane().setDefaultButton(importButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    private Dictionary createDictionary(Language from, Language to, String dictionaryName, boolean special, String smallIconPath, String bigIconPath) {
        File smallIconFile = new File(smallIconPath);
        byte[] smallIconFileByteArray = new byte[(int) smallIconFile.length()];

        File bigIconFile = new File(bigIconPath);
        byte[] bigIconFileByteArray = new byte[(int) bigIconFile.length()];

        try {
            FileInputStream fileInputStream = new FileInputStream(bigIconFile);
            //convert bigIconFile into array of bytes
            fileInputStream.read(bigIconFileByteArray);
            fileInputStream.close();

            fileInputStream = new FileInputStream(smallIconFile);
            fileInputStream.read(smallIconFileByteArray);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        Dictionary dictionary = DictionaryServiceImpl.getInstance().createDictionary(from, to, dictionaryName, special, smallIconFileByteArray, bigIconFileByteArray);
        return dictionary;
    }

    private void attachImporter(SupportedFileType type) {
        assert type != null : "type != null";

        if (selectedFileType == type) return;

        switch (type) {
            case BGOFFICE:
                importer = new BgOfficeImporter();
                break;
            case BABYLON:
                importer = new BabylonTxtImporter();
                break;
        }
    }

    public static void main(String[] args) {
        ImportDialog tImportDialog = new ImportDialog(null, true);
        tImportDialog.showDialog();
    }

    public interface DictionaryImporter {
        void importDictionary(Language from, Language to, String dictionaryName, boolean special, String smallIconPath, String bigIconPath, String fileName);
    }

    private class BgOfficeImporter implements DictionaryImporter {

        @Override
        public void importDictionary(Language from, Language to, String dictionaryName, boolean special, String smallIconPath, String bigIconPath, String fileName) {
            getLogger().info("import started");

            Dictionary dictionary = createDictionary(from, to, dictionaryName, special, smallIconPath, bigIconPath);

            List<DictionaryEntry> tDictionaryEntries = new ArrayList<DictionaryEntry>();

            try {
                RandomAccessFile file = new RandomAccessFile(fileName, "r");

                //first byte in the data smallIconFile is '\0'
                byte nullByte = file.readByte();

                while (true) {
                    try {
                        byte[] record = new byte[BUFFER_SIZE];

                        int i = 0;

                        while (true) {
                            byte byteRead = file.readByte();

                            if (byteRead == nullByte) {
                                break;
                            }

                            record[i++] = byteRead;
                        }

                        byte[] copy = Arrays.copyOf(record, i);

                        Charset charset = Charset.forName("CP1251");
                        CharsetDecoder decoder = charset.newDecoder();

                        CharBuffer charBuffer = decoder.decode(ByteBuffer.wrap(copy));

                        String[] lines = charBuffer.toString().split("\n");

                        String translation = "";

                        if (lines.length > 1) {
                            for (int j = 1; j < lines.length; j++) {
                                translation += lines[j] + "\n";
                            }
                        }

                        //getLogger().info("Adding word " + lines[0] + "; translation - " + translation + "\n");

                        DictionaryEntry tDictionaryEntry = new DictionaryEntry();
                        tDictionaryEntry.setWord(lines[0]);
                        tDictionaryEntry.setTranslation(translation);
                        tDictionaryEntry.setDictionary(dictionary);

                        tDictionaryEntries.add(tDictionaryEntry);
                    } catch (EOFException e) {
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (CharacterCodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            DictionaryServiceImpl.getInstance().addWords(tDictionaryEntries);

            getLogger().info("import finished");
        }
    }

    private class BabylonTxtImporter implements DictionaryImporter {

        @Override
        public void importDictionary(Language from, Language to, String dictionaryName, boolean special, String smallIconPath, String bigIconPath, String fileName) {
            getLogger().info("import started");

            Dictionary dictionary = createDictionary(from, to, dictionaryName, special, smallIconPath, bigIconPath);
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(fileName));
                List<DictionaryEntry> tDictionaryEntries = new ArrayList<DictionaryEntry>();
                String line;
                boolean shouldExit = false;
                int count = 0;
                while (true) {
                    //Word
                    if ((line = in.readLine()) == null || line.isEmpty()) shouldExit = true;
                    String word = line;
                    //Translation
                    if ((line = in.readLine()) == null || line.isEmpty()) shouldExit = true;
                    String oneLineTranslation = line;
                    //Empty line
                    if ((line = in.readLine()) == null) shouldExit = true;


                    if (word != null && oneLineTranslation != null) {
                        String translation = "";
                        for (String tLine : oneLineTranslation.split("[;]+")) {
                            translation += tLine + "\n";
                        }

                        DictionaryEntry tDictionaryEntry = new DictionaryEntry();
                        tDictionaryEntry.setWord(word);
                        tDictionaryEntry.setTranslation(translation);
                        tDictionaryEntry.setDictionary(dictionary);
                        tDictionaryEntry.setUpdatedByUser(false);

                        tDictionaryEntries.add(tDictionaryEntry);
                    }

                    if (shouldExit) {
                        DictionaryServiceImpl.getInstance().addWords(tDictionaryEntries);
                        return;
                    }
                }
            } catch (IOException e) {
                getLogger().error(e.getMessage(), e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        getLogger().error(e.getMessage(), e);
                    }
                }
            }
        }
    }
}
