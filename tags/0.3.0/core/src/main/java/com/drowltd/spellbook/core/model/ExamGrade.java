package com.drowltd.spellbook.core.model;

import com.drowltd.spellbook.core.i18n.Translator;

/**
 * Description goes here...
 *
 * @author Bozhidar Batsov
 * @since 3.3
 */
public enum ExamGrade {
    EXCELLENT("Excellent"), VERY_GOOD("VeryGood"), GOOD("Good"), AVERAGE("Average"), POOR("Poor");

    public static final Translator TRANSLATOR = Translator.getTranslator("Model");

    private String name;

    private ExamGrade(String name) {
        this.name = name;
    }

    public static ExamGrade getGrade(int score) {
        if (score >= 90) {
            return EXCELLENT;
        } else if (score >= 80) {
            return VERY_GOOD;
        } else if (score >= 70) {
            return GOOD;
        } else if (score >= 60) {
            return AVERAGE;
        } else {
            return POOR;
        }
    }

    @Override
    public String toString() {
        return TRANSLATOR.translate(name + "(Grade)");
    }
}
