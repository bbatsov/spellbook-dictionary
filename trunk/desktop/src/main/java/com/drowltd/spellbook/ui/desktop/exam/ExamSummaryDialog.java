package com.drowltd.spellbook.ui.desktop.exam;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.ExamGrade;
import com.drowltd.spellbook.core.model.ExamScoreEntry;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.service.exam.ExamService;
import com.drowltd.spellbook.ui.swing.model.ListBackedListModel;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.drowltd.spellbook.ui.swing.util.SwingUtil;
import com.drowltd.spellbook.ui.swing.validation.ButtonControllingDocumentListener;
import com.drowltd.spellbook.util.DateUtils;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author Bozhidar Batsov
 * @author miroslava
 */
public class ExamSummaryDialog extends StandardDialog {

    private static final Translator TRANSLATOR = Translator.getTranslator("ExamSummaryDialog");
    private ExamService examService = ExamService.getInstance();
    private DictionaryService dictionaryService = DictionaryService.getInstance();

    private JLabel incorrectWords;
    private JLabel correctWords;
    private JLabel message;
    private JLabel iconLabel;
    private JLabel score;
    private JLabel grade;
    private JLabel totalWords;
    private JLabel totalTime;
    private JLabel averageTime;
    private JTextField nameTextField;
    private JButton submitScoreButton;
    private JPanel incorrectWordPanel;
    private JPanel scoreboardPanel;
    private ExamStats examStats;
    private Dialog owner;
    private static final int MIN_PASSING_SCORE = 60;
    private ScoreboardTableModel scoreboardTableModel;
    private boolean submitted = false;

    public ExamSummaryDialog(final Dialog owner, final ExamStats examStats) {
        super(owner, true);
        TRANSLATOR.reset();

        this.examStats = examStats;
        this.owner = owner;

        incorrectWords = new JLabel();
        correctWords = new JLabel();
        totalTime = new JLabel();
        averageTime = new JLabel();
        grade = new JLabel();
        totalWords = new JLabel();
        message = new JLabel();
        iconLabel = new JLabel();
        score = new JLabel();
        nameTextField = new JTextField();
        submitScoreButton = new JButton(TRANSLATOR.translate("SubmitScore(Button)"));
        nameTextField.getDocument().addDocumentListener(new ButtonControllingDocumentListener(nameTextField, submitScoreButton));

        submitScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!submitted) {
                final ExamScoreEntry tEntry = examStats.createExamScoreEntry(nameTextField.getText());
                examService.addScoreboardResult(tEntry);
                scoreboardTableModel.getScoreEntries().add(tEntry);
                scoreboardTableModel.fireTableRowsInserted(0, scoreboardTableModel.getRowCount());
                submitted = true;
                } else {
                    JOptionPane.showMessageDialog(ExamSummaryDialog.this, TRANSLATOR.translate("ScoreAlreadySubmitted(Message)"));
                }
            }
        });

        setTitle(TRANSLATOR.translate("ExamSummaryDialog(Title)"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(IconManager.getImageIcon("teacher.png", IconManager.IconSize.SIZE16).getImage());


        incorrectWordPanel = createIncorrectWordsPanel();
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
                incorrectWordPanel = createIncorrectWordsPanel();
                add(incorrectWordPanel, JideBoxLayout.FIX);
                incorrectWordPanel.setVisible(false);
                scoreboardPanel = createScoreboardPanel();
                add(scoreboardPanel, JideBoxLayout.FIX);
                scoreboardPanel.setVisible(false);
            }
        };
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[][grow]"));

        panel.add(message);
        panel.add(iconLabel);
        panel.add(new JLabel(TRANSLATOR.translate("Score(Label)")));
        panel.add(score);
        panel.add(new JLabel(TRANSLATOR.translate("Grade(Label)")));
        panel.add(grade);
        panel.add(new JLabel(TRANSLATOR.translate("Total(Label)")));
        panel.add(totalWords);
        panel.add(new JLabel(TRANSLATOR.translate("NumberOfCorrect(Label)")));
        panel.add(correctWords);
        panel.add(new JLabel(TRANSLATOR.translate("NumberOfIncorrect(Label)")));
        panel.add(incorrectWords);
        panel.add(new JLabel(TRANSLATOR.translate("TotalTime(Label)")));
        panel.add(totalTime);
        panel.add(new JLabel(TRANSLATOR.translate("AvgTime(Label)")));
        panel.add(averageTime);
        panel.add(new JLabel(TRANSLATOR.translate("EnterName(Label)")));
        panel.add(nameTextField, "w 150, growx, split 2");
        panel.add(submitScoreButton);

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        JButton closeButton = new JButton();
        JButton incorrectWordsButton = new JButton();
        JButton scoreboardButton = new JButton();
        incorrectWordsButton.setMnemonic('D');
        closeButton.setName(OK);
        buttonPanel.addButton(closeButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(incorrectWordsButton, ButtonPanel.OTHER_BUTTON);
        buttonPanel.addButton(scoreboardButton, ButtonPanel.OTHER_BUTTON);

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
                if (incorrectWordPanel.isVisible()) {
                    incorrectWordPanel.setVisible(false);
                    putValue(Action.NAME, "View incorrect <<");
                    pack();
                } else {
                    incorrectWordPanel.setVisible(true);
                    putValue(Action.NAME, "<< View incorrect");
                    pack();
                }
            }
        });

        scoreboardButton.setAction(new AbstractAction("View scoreboard >>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (scoreboardPanel.isVisible()) {
                    scoreboardPanel.setVisible(false);
                    putValue(Action.NAME, "View scoreboard <<");
                    pack();
                } else {
                    scoreboardPanel.setVisible(true);
                    putValue(Action.NAME, "<< View scoreboard");
                    pack();
                }
            }
        });

        setDefaultCancelAction(closeButton.getAction());
        setDefaultAction(closeButton.getAction());
        getRootPane().setDefaultButton(closeButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setSizeConstraint(ButtonPanel.NO_LESS_THAN); // since the checkbox is quite wide, we don't want all of them have the same size.
        submitScoreButton.setEnabled(false);
        return buttonPanel;
    }

    public JPanel createIncorrectWordsPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[100:150:, grow 40][200:300:, grow 60]", "[grow]"));
        panel.setName("IW");

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

    public JPanel createScoreboardPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap", "[grow]", "[200]"));
        panel.setName("SB");

        List<ExamScoreEntry> examScoreEntryList = examService.getExamScores();

        scoreboardTableModel = new ScoreboardTableModel(examScoreEntryList);
        panel.add(new JScrollPane(new JTable(scoreboardTableModel)), "grow");

        return panel;
    }

    public void showExamResult() {
        int correctWords = examStats.getCorrectWords().size();
        int totalWords = examStats.getTotalWords();

        int score = (correctWords * 100) / totalWords;

        this.score.setText(Integer.toString(score) + "%");
        this.grade.setText(ExamGrade.getGrade(score).toString());
        this.correctWords.setText(Integer.toString(correctWords));
        this.incorrectWords.setText(Integer.toString(examStats.getIncorrectWords().size()));
        this.totalWords.setText(Integer.toString(totalWords));
        this.totalTime.setText(DateUtils.dateDifference(examStats.getStartTime(), examStats.getEndTime()));
        this.averageTime.setText(DateUtils.getAvgDuration(examStats.getStartTime(), examStats.getEndTime(), totalWords));

        if (score > MIN_PASSING_SCORE) {
            message.setText(TRANSLATOR.translate("Passed(Label)"));
            iconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE48));
        } else {
            message.setText(TRANSLATOR.translate("Failed(Label)"));
            iconLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE48));
        }

        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}

class ScoreboardTableModel extends AbstractTableModel {
    private String[] columnNames = {"Name", "From", "To", "Score"};
    private List<ExamScoreEntry> scoreEntries;

    ScoreboardTableModel(final List<ExamScoreEntry> pScoreEntries) {
        scoreEntries = pScoreEntries;
    }

    public List<ExamScoreEntry> getScoreEntries() {
        return scoreEntries;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return scoreEntries.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return scoreEntries.get(row).toArray()[col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
}
