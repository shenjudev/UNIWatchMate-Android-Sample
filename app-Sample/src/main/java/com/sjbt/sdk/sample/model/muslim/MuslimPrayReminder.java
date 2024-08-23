package com.sjbt.sdk.sample.model.muslim;

import java.util.List;

public class MuslimPrayReminder {
    private int version;
    private int switchState;
    private List<MuslimPrayRemind> prayReminds;

    public MuslimPrayReminder(int version, int switchState, List<MuslimPrayRemind> prayReminds) {
        this.version = version;
        this.switchState = switchState;
        this.prayReminds = prayReminds;
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

    public List<MuslimPrayRemind> getPrayReminds() {
        return prayReminds;
    }

    public void setPrayReminds(List<MuslimPrayRemind> muslimPrayReminds) {
        this.prayReminds = muslimPrayReminds;
    }
}
