package com.sjbt.sdk.sample.model.muslim;

public class MuslimPrayRemind {
    private int id;
    private boolean open;

    public static final int PRAY_FAJR_ID = 0;
    public static final int PRAY_SUNRISE_ID = 1;
    public static final int PRAY_DHUHR_ID = 2;
    public static final int PRAY_ASR_ID = 3;
//    public static final int PRAY_SUNSET_ID = 4;
    public static final int PRAY_MAGHRIB_ID = 4;
    public static final int PRAY_ISHA_ID = 5;

    public MuslimPrayRemind(int id, boolean open) {
        this.id = id;
        this.open = open;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
