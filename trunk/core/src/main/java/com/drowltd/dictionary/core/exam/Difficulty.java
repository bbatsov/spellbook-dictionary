package com.drowltd.dictionary.core.exam;

/**
 *
 * @author F_R_A_N_K_Y
 */
public enum Difficulty {
    EASY(30, Integer.SIZE), MEDIUM(10,30), HARD(1,10);

    private final int low;
    private final int high;

    private Difficulty(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public int getLow(){
        return low;
    }

    public int getHigh() {
        return high;
    }
}
