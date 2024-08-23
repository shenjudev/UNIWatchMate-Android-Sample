package com.sjbt.sdk.sample.model.muslim;

import java.util.List;

public class MuslimTasbihReminder {

    private int version;
    private int switchState;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private int interval;
    private List<MuslimRepeatRule> muslimRepeatRules;

    public static final int INTERVAL_30_MINS = 30;
    public static final int INTERVAL_60_MINS = 60;
    public static final int INTERVAL_120_MINS = 120;
    public static final int INTERVAL_180_MINS = 180;

    @Override
    public MuslimTasbihReminder clone(){
        MuslimTasbihReminder temp = null;
        try {
            temp = (MuslimTasbihReminder) super.clone();
        }catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return temp;
    }

    public MuslimTasbihReminder(int version, int switchState, int startHour, int startMinute, int endHour, int endMinute, int interval, List<MuslimRepeatRule> muslimRepeatRules) {
        this.version = version;
        this.switchState = switchState;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.interval = interval;
        this.muslimRepeatRules = muslimRepeatRules;
    }

    // Getters and Setters
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSwitchState() {
        return switchState;
    }

    public void setSwitchState(int switchState) {
        this.switchState = switchState;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public List<MuslimRepeatRule> getRepeatRules() {
        return muslimRepeatRules;
    }

    public void setRepeatRules(List<MuslimRepeatRule> muslimRepeatRules) {
        this.muslimRepeatRules = muslimRepeatRules;
    }
}
