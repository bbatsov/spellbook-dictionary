package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.ui.swing.component.BaseDialog;
import com.jidesoft.dialog.ButtonPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
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
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;

/**
 * Import dictionary dialog.
 *
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class ImportDialog extends BaseDialog {
    private static final DictionaryService DICTIONARY_SERVICE = DictionaryService.getInstance();
    private JTextField dictionaryFileTextField;
    private JComboBox fileFormatComboBox;
    private JTextField dictionaryNameTextField;
    private JTextField dictionaryIconTextField;

    public ImportDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
    }

    @Override
    public JComponent createContentPanel() {
        JPanel mainPanel = new JPanel(new MigLayout("wrap 3", "[grow][grow][grow]", "[][][]"));

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryFile(Label)")), "grow");

        dictionaryFileTextField = new JTextField();
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

        this.fileFormatComboBox = fileFormatComboBox;
        mainPanel.add(this.fileFormatComboBox, "span2, growx");

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryType(Label)")), "growx");

        JRadioButton normalRadionButton = new JRadioButton("Normal");

        mainPanel.add(normalRadionButton);

        JRadioButton specialRadioButton = new JRadioButton("Special");

        mainPanel.add(specialRadioButton);

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryName(Label)")), "growx");

        dictionaryNameTextField = new JTextField();

        mainPanel.add(dictionaryNameTextField, "span 2, growx, width 200::");

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryIcon(Label)")), "growx");

        dictionaryIconTextField = new JTextField();

        mainPanel.add(dictionaryIconTextField, "growx, width 200::");

        JButton selectIconButton = new JButton(getTranslator().translate("SelectIcon(Button)"));

        mainPanel.add(selectIconButton, "growx");

        return mainPanel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel(SwingConstants.RIGHT);

        JButton importButton = new JButton();

        importButton.setAction(new AbstractAction(getTranslator().translate("Import(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                importDictionary(Language.ENGLISH, Language.ENGLISH, "Computer terminology", "laptop.png", dictionaryFileTextField.getText());
            }
        });

        JButton closeButton = new JButton();
        closeButton.setAction(new AbstractAction(getBaseTranslator().translate("Close(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
            }
        });

        buttonPanel.addButton(importButton);
        buttonPanel.addButton(closeButton);

        setDefaultCancelAction(closeButton.getAction());
        setDefaultAction(importButton.getAction());
        getRootPane().setDefaultButton(importButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    private void importDictionary(Language from, Language to, String dictionaryName, String iconName, String fileName) {
        Dictionary dictionary = DICTIONARY_SERVICE.createDictionary(from, to, dictionaryName, iconName);

        try {
            RandomAccessFile file = new RandomAccessFile(fileName, "r");

            //first byte in the data file is '\0'
            byte nullByte = file.readByte();

            while (true) {
                try {
                    byte[] record = new byte[20000];

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

                    getLogger().info("Adding word " + lines[0] + "; translation - " + translation + "\n");
                    DICTIONARY_SERVICE.addWord(lines[0], translation, dictionary);

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


    }
}
