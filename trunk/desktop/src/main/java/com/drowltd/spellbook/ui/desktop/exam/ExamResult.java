package com.drowltd.spellbook.ui.desktop.exam;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.ScoreboardEntry;
import com.drowltd.spellbook.core.service.exam.ExamService;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.drowltd.spellbook.ui.swing.util.SwingUtil;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.plaf.UIDefaultsLookup;
import net.miginfocom.swing.MigLayout;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author miroslava
 */
public class ExamResult extends StandardDialog {

    private static final Translator TRANSLATOR = Translator.getTranslator("ExamResult");
    private boolean scoreboardVisibility = false;
    private ExamService examService;
    private JLabel correctWordsLabel;
    private JLabel correctWrodsResultLabel;
    private JLabel endLabel;
    private EntityManager entityManager;
    private JLabel iconLabel;
    private JLabel jLabel1;
    private JScrollPane jScrollPane1;
    private JButton okResultButton;
    private JLabel result;
    private JTable scoreboarTable;
    private JLabel scoreboardButton;
    private List<ScoreboardEntry> scoreboardEntryList;
    private Query scoreboardEntryQuery;
    private JTextField scoreboardNameField;
    private JButton seeWrongWords;
    private JButton submitScoreButton;
    private JLabel successRateLabel;
    private JLabel successRateResultLabel;
    private JLabel wrongWordsLabel;
    private JLabel wrongWrodsResultLabel;

    public ExamResult(Frame parent, boolean modal) {
        super(parent, modal);
        TRANSLATOR.reset();

        examService = new ExamService();


//       entityManager = java.beans.Beans.isDesignTime() ? null : javax.persistence.Persistence.createEntityManagerFactory("jdbc:h2:F:\\opt\\spellbook\\db\\spellbookPU").createEntityManager();
//        scoreboardEntryQuery = java.beans.Beans.isDesignTime() ? null : entityManager.createQuery("SELECT s FROM ScoreboardEntry s");
//        scoreboardEntryList = java.beans.Beans.isDesignTime() ? java.util.Collections.emptyList() : scoreboardEntryQuery.getResultList();

        correctWordsLabel = new JLabel();
        correctWrodsResultLabel = new JLabel();
        wrongWordsLabel = new JLabel();
        wrongWrodsResultLabel = new JLabel();
        successRateLabel = new JLabel();
        successRateResultLabel = new JLabel();
        endLabel = new JLabel();

        iconLabel = new JLabel();
        result = new JLabel();
//        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new JLabel();
        scoreboardNameField = new JTextField();
        submitScoreButton = new JButton();
        scoreboardButton = new JLabel();
//        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new JScrollPane();
        scoreboarTable = new JTable();

        setLocationRelativeTo(parent);
        setTitle("ExamResult(Title)");
        setResizable(false);
        setSize(279, 342);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(IconManager.getImageIcon("teacher.png", IconManager.IconSize.SIZE16).getImage());

        initComponents();

    }

    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("", "[]", "[][][][][][][]"));
        panel.add(scoreboardButton, "wrap, right");
        scoreboardButton.setForeground(new Color(0, 0, 255));
        scoreboardButton.setHorizontalAlignment(SwingConstants.RIGHT);
        scoreboardButton.setText(TRANSLATOR.translate("ShowScoreboard(JLabel)"));
        scoreboardButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        scoreboardButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        scoreboardButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                scoreboardButtonMouseClicked(evt);
            }
        });

        panel.add(jLabel1, "wrap, left");
        jLabel1.setText(TRANSLATOR.translate("ScoreboardName(JTextFiedl)"));

        panel.add(scoreboardNameField, "split 2, w 100!");
        scoreboardNameField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                scoreboardNameFieldActionPerformed(evt);
            }
        });
        panel.add(submitScoreButton, "wrap");
        submitScoreButton.setText(TRANSLATOR.translate("SubmitScore(JButton)"));
        submitScoreButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                submitScoreButtonActionPerformed(evt);
            }
        });

        panel.add(result, "center, wrap");
        result.setFont(new Font("Tahoma", 1, 14));
        result.setText(TRANSLATOR.translate("Result(JLable)"));

        panel.add(correctWordsLabel, "split 3");
        correctWordsLabel.setFont(new Font("Tahoma", 1, 11));
        correctWordsLabel.setHorizontalAlignment(SwingConstants.LEFT);
        correctWordsLabel.setText(TRANSLATOR.translate("CorrectWords(String)"));
        panel.add(correctWrodsResultLabel, "");

        panel.add(iconLabel, "spany 3, wrap, center");
        iconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE48));

        panel.add(wrongWordsLabel, "split 3");
        wrongWordsLabel.setFont(new Font("Tahoma", 1, 11));
        wrongWordsLabel.setHorizontalAlignment(SwingConstants.LEFT);
        wrongWordsLabel.setText(TRANSLATOR.translate("WrongWords(String)"));
        panel.add(wrongWrodsResultLabel, "wrap");

        panel.add(successRateLabel, "split 3");
        successRateLabel.setFont(new Font("Tahoma", 1, 11));
        successRateLabel.setText(TRANSLATOR.translate("Success(Label)"));
        panel.add(successRateResultLabel, "wrap");

        panel.add(endLabel, "wrap, center");

        panel.add(scoreboarTable, "dock east");
        scoreboarTable.setModel(new DefaultTableModel(
                new Object[][]{
                        {new Integer(1), null, null},
                        {new Integer(2), null, null},
                        {new Integer(3), null, null},
                        {new Integer(4), null, null},
                        {new Integer(5), null, null},
                        {new Integer(6), null, null},
                        {new Integer(7), null, null},
                        {new Integer(8), null, null},
                        {new Integer(9), null, null},
                        {new Integer(10), null, null},
                        {new Integer(11), null, null},
                        {new Integer(12), null, null},
                        {new Integer(13), null, null},
                        {new Integer(14), null, null},
                        {new Integer(15), null, null}
                },
                new String[]{
                        "", "Name", "Result"
                }) {

            Class[] types = new Class[]{
                    Integer.class, String.class, String.class
            };
            boolean[] canEdit = new boolean[]{
                    false, false, false
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        scoreboarTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(scoreboarTable);
        scoreboarTable.getColumnModel().getColumn(0).setMinWidth(20);
        scoreboarTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        scoreboarTable.getColumnModel().getColumn(0).setMaxWidth(20);
        scoreboarTable.getColumnModel().getColumn(1).setResizable(false);
        scoreboarTable.getColumnModel().getColumn(2).setResizable(false);

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        okResultButton = new JButton();
        seeWrongWords = new JButton();
        // seeWrongWords.setName(TRANSLATOR.translate("ShowWords(Button)"));
        okResultButton.setName(OK);
        buttonPanel.addButton(okResultButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(seeWrongWords, ButtonPanel.OTHER_BUTTON);
        okResultButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.okButtonText")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_AFFIRMED);
                setVisible(false);
                dispose();
//                setVisible(false);
//        ExamDialog.setFeedbackFieldDefault();
            }
        });

        seeWrongWords.setText(TRANSLATOR.translate("ShowWords(Button)"));
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/ExamResult");
        seeWrongWords.setAction(new AbstractAction(bundle.getString("ShowWords(Button)")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                WrongWordsDialog wrongWordsDialog = new WrongWordsDialog(null, true);
                wrongWordsDialog.setLocationRelativeTo(null);
                wrongWordsDialog.setVisible(true);
            }
        });

        return buttonPanel;
    }

    private void scoreboardButtonMouseClicked(MouseEvent evt) {
        scoreboardViewStatus();
    }

    private void scoreboardNameFieldActionPerformed(ActionEvent evt) {
        if (!scoreboardNameField.getText().isEmpty()) {
            scoreboardFilling();
        } else {
            SwingUtil.showBalloonTip(scoreboardNameField, TRANSLATOR.translate("EnterName(BallonTip)"));
        }
    }

    private void submitScoreButtonActionPerformed(ActionEvent evt) {
        if (!scoreboardNameField.getText().isEmpty()) {
            scoreboardFilling();
        } else {
            SwingUtil.showBalloonTip(scoreboardNameField, TRANSLATOR.translate("EnterName(BallonTip)"));
        }
    }

    public void showExamResult(ExamStats examStats) {
        int correctWords = examStats.getCorrectWords().size();
        int totalWords = examStats.getIncorrectWords().size() + correctWords;

        setSize(279, 342);

        int grade = (correctWords * 100) / totalWords;

        correctWrodsResultLabel.setText(Integer.toString(correctWords));
        wrongWrodsResultLabel.setText(Integer.toString(totalWords - correctWords));
        successRateResultLabel.setText(Integer.toString(grade) + "%");

        if (grade > 60) {
            endLabel.setText(TRANSLATOR.translate("Passed(Label)"));
            iconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE48));
        } else {
            endLabel.setText(TRANSLATOR.translate("Failed(Label)"));
            iconLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE48));
        }
        setVisible(true);
    }

    public void scoreboardViewStatus() {

        if (scoreboardVisibility == false) {
            scoreboardButton.setText(TRANSLATOR.translate("HideScoreboard(JLabel)"));
            scoreboardVisibility = true;
            setTable();
            setSize(544, 342);
        } else {
            scoreboardButton.setText(TRANSLATOR.translate("ShowScoreboard(JLabel)"));
            setSize(279, 342);
            scoreboardVisibility = false;
        }

    }

    public void scoreboardFilling() {
        addScore();
        scoreboardNameField.setText(null);

        if (scoreboardVisibility == false) {
            scoreboardViewStatus();
        } else {
            setTable();
        }

    }

    public void addScore() {

        Double examWordsForScoreboard = Double.parseDouble(correctWrodsResultLabel.getText())
                + Double.parseDouble(wrongWrodsResultLabel.getText());
        examService.addScoreboardResult(scoreboardNameField.getText(), examWordsForScoreboard,
                Double.parseDouble(wrongWrodsResultLabel.getText()), null);

    }

    public void setTable() {
        int i = 0;
        try {
            while (!examService.getScoreboardUsername().get(i).isEmpty()) {
                scoreboarTable.setValueAt(examService.getScoreboardUsername().get(i), i, 1);
                scoreboarTable.setValueAt(Math.floor(successRateForTable(i))
                        + "% (" + examService.getScoreboardDifficulty().get(i) + ")", i, 2);

                if (i == 14) {
                    break;
                }
                i++;
            }
        } catch (Exception e) {
        }

    }

    public double successRateForTable(int sr) {
        return (((((examService.getScoreboardExamword().get(sr)) - (examService.getScoreboardWrongword().get(sr))))
                * 100) / examService.getScoreboardExamword().get(sr));
    }
}
