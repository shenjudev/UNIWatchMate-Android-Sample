package com.sjbt.sdk.sample.model.muslim;

public class MuslimRepeatRule {
    private int weekId;
    private boolean repeat;

    public static final int SUN = 0;
    public static final int MON = 1;
    public static final int TUE = 2;
    public static final int WED = 3;
    public static final int THU = 4;
    public static final int FRI = 5;
    public static final int SAT = 6;

    public MuslimRepeatRule(int weekId, boolean repeat) {
        this.weekId = weekId;
        this.repeat = repeat;
    }

    // Getters and Setters
    public int getWeekId() {
        return weekId;
    }

    public void setWeekId(int weekId) {
        this.weekId = weekId;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}
