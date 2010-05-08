package com.drowltd.spellbook.ui.desktop.exam;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.service.exam.ExamService;
import com.drowltd.spellbook.ui.swing.model.ListBackedListModel;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.drowltd.spellbook.ui.swing.util.SwingUtil;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.dialog.StandardDialogPane;
import com.jidesoft.swing.JideBoxLayout;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;

/**
 * @author miroslava
 */
public class ExamResult extends StandardDialog {

    private static final Translator TRANSLATOR = Translator.getTranslator("ExamResult");
    private ExamService examService = ExamService.getInstance();
    private DictionaryService dictionaryService = DictionaryService.getInstance();

    private JLabel correctWordsLabel;
    private JLabel correctWordsResultLabel;
    private JLabel endLabel;
    private JLabel iconLabel;
    private JLabel jLabel1;
    private JLabel result;
    private JLabel successRateLabel;
    private JLabel successRateResultLabel;
    private JLabel wrongWordsLabel;
    private JLabel wrongWordsResultLabel;
    private JPanel incorrectWordsPanel;
    private JPanel scoreboardPanel;
    private ExamStats examStats;
    private Dialog owner;
    private static final int MIN_PASSING_GRADE = 60;

    public ExamResult(Dialog owner, ExamStats examStats) {
        super(owner, true);
        TRANSLATOR.reset();

        this.examStats = examStats;
        this.owner = owner;

        correctWordsLabel = new JLabel();
        correctWordsResultLabel = new JLabel();
        wrongWordsLabel = new JLabel();
        wrongWordsResultLabel = new JLabel();
        successRateLabel = new JLabel();
        successRateResultLabel = new JLabel();
        endLabel = new JLabel();

        iconLabel = new JLabel();
        result = new JLabel();
        jLabel1 = new JLabel();

        setTitle("ExamResult(Title)");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(IconManager.getImageIcon("teacher.png", IconManager.IconSize.SIZE16).getImage());

        incorrectWordsPanel = createIncorrectWordsPanel();
    }

    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    protected StandardDialogPane createStandardDialogPane() {
        return new DefaultStandardDialogPane() {
            @Override
            protected void layoutComponents(Component bannerPanel, Component contentPanel, ButtonPanel buttonPanel) {
                setLayout(new JideBoxLayout(this, BoxLayout.Y_AXIS));
                if (bannerPanel != null) {
                    add(bannerPanel);
                }
                if (contentPanel != null) {
                    add(contentPanel);
                }
                add(buttonPanel, JideBoxLayout.FIX);
                incorrectWordsPanel = createIncorrectWordsPanel();
                add(incorrectWordsPanel, JideBoxLayout.VARY);
                incorrectWordsPanel.setVisible(false);
            }
        };
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("", "[]", "[][][][][][][]"));

        panel.add(jLabel1, "wrap, left");
        jLabel1.setText(TRANSLATOR.translate("ScoreboardName(JTextFiedl)"));

        panel.add(result, "center, wrap");
        result.setFont(new Font("Tahoma", 1, 14));
        result.setText(TRANSLATOR.translate("Result(JLable)"));

        panel.add(correctWordsLabel, "split 3");
        correctWordsLabel.setFont(new Font("Tahoma", 1, 11));
        correctWordsLabel.setHorizontalAlignment(SwingConstants.LEFT);
        correctWordsLabel.setText(TRANSLATOR.translate("CorrectWords(String)"));
        panel.add(correctWordsResultLabel, "");

        panel.add(iconLabel, "spany 3, wrap, center");
        iconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE48));

        panel.add(wrongWordsLabel, "split 3");
        wrongWordsLabel.setFont(new Font("Tahoma", 1, 11));
        wrongWordsLabel.setHorizontalAlignment(SwingConstants.LEFT);
        wrongWordsLabel.setText(TRANSLATOR.translate("WrongWords(String)"));
        panel.add(wrongWordsResultLabel, "wrap");

        panel.add(successRateLabel, "split 3");
        successRateLabel.setFont(new Font("Tahoma", 1, 11));
        successRateLabel.setText(TRANSLATOR.translate("Success(Label)"));
        panel.add(successRateResultLabel, "wrap");

        panel.add(endLabel, "wrap, center");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        JButton closeButton = new JButton();
        JButton incorrectWordsButton = new JButton();
        incorrectWordsButton.setMnemonic('D');
        closeButton.setName(OK);
        buttonPanel.addButton(closeButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(incorrectWordsButton, ButtonPanel.OTHER_BUTTON);

        closeButton.setAction(new AbstractAction("Close") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_AFFIRMED);
                setVisible(false);
                dispose();
            }
        });

        incorrectWordsButton.setAction(new AbstractAction("View incorrect >>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (incorrectWordsPanel.isVisible()) {
                    incorrectWordsPanel.setVisible(false);
                    putValue(Action.NAME, "View incorrect <<");
                    pack();
                } else {
                    incorrectWordsPanel.setVisible(true);
                    putValue(Action.NAME, "<< View incorrect");
                    pack();
                }
            }
        });

        setDefaultCancelAction(closeButton.getAction());
        setDefaultAction(closeButton.getAction());
        getRootPane().setDefaultButton(closeButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setSizeConstraint(ButtonPanel.NO_LESS_THAN); // since the checkbox is quite wide, we don't want all of them have the same size.
        return buttonPanel;
    }

    public JPanel createIncorrectWordsPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[100:150:, grow 40][200:300:, grow 60]", "[grow]"));

        final JList incorrectWords = new JList(new ListBackedListModel(examStats.getIncorrectWords()));
        panel.add(new JScrollPane(incorrectWords), "grow");

        final JTextPane translationPane = new JTextPane();
        translationPane.setContentType("text/html");
        panel.add(new JScrollPane(translationPane), "grow");

        incorrectWords.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                final String selectedWord = (String) incorrectWords.getSelectedValue();
                translationPane.setText(SwingUtil.formatTranslation(selectedWord,
                        dictionaryService.getTranslation(selectedWord, examStats.getDictionary())));
            }
        });

        return panel;
    }

    public void showExamResult() {
        int correctWords = examStats.getCorrectWords().size();
        int totalWords = examStats.getIncorrectWords().size() + correctWords;

        int grade = (correctWords * 100) / totalWords;

        correctWordsResultLabel.setText(Integer.toString(correctWords));
        wrongWordsResultLabel.setText(Integer.toString(totalWords - correctWords));
        successRateResultLabel.setText(Integer.toString(grade) + "%");

        if (grade > MIN_PASSING_GRADE) {
            endLabel.setText(TRANSLATOR.translate("Passed(Label)"));
            iconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE48));
        } else {
            endLabel.setText(TRANSLATOR.translate("Failed(Label)"));
            iconLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE48));
        }

        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}
