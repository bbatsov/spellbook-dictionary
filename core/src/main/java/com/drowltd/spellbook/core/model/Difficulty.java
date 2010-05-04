package com.drowltd.spellbook.core.model;

import com.drowltd.spellbook.core.i18n.Translator;

/**
 *
 * @author Ivan Spasov
 * @since 0.2
 */
public enum Difficulty {
    EASY(30, Integer.MAX_VALUE, 45), MEDIUM(10,30, 30), HARD(1,10, 15);

    private final int low;
    private final int high;
    private final int time; // in seconds
    private static final Translator TRANSLATOR = Translator.getTranslator("PreferencesDialog");

    private Difficulty(int low, int high, int time) {
        this.low = low;
        this.high = high;
        this.time = time;
    }

    public int getLow(){
        return low;
    }

    public int getHigh() {
        return high;
    }

    public String getName() {
        switch (this) {
            case EASY: return TRANSLATOR.translate("Easy(Label)");
            case MEDIUM: return TRANSLATOR.translate("Medium(Label)");
            case HARD: return TRANSLATOR.translate("Hard(Label)");
            default: return null;
        }
    }

    public int getTime() {
        return time;
    }
}
