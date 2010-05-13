package com.drowltd.spellbook.core.model;

import com.drowltd.spellbook.core.i18n.Translator;

/**
 * @author Ivan Spasov
 * @since 0.2
 */
public enum Difficulty {
    EASY(30, Integer.MAX_VALUE, 45), MEDIUM(10, 30, 30), HARD(1, 10, 15);

    private final int low;
    private final int high;
    private final int time; // in seconds
    private static final Translator TRANSLATOR = Translator.getTranslator("Model");

    private Difficulty(int low, int high, int time) {
        this.low = low;
        this.high = high;
        this.time = time;
    }

    public int getLow() {
        return low;
    }

    public int getHigh() {
        return high;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        switch (this) {
            case EASY:
                return TRANSLATOR.translate("Easy(Difficulty)");
            case MEDIUM:
                return TRANSLATOR.translate("Medium(Difficulty)");
            case HARD:
                return TRANSLATOR.translate("Hard(Difficulty)");
            default:
                return null;
        }
    }
}
