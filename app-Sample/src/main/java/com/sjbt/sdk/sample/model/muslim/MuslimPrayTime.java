package com.sjbt.sdk.sample.model.muslim;

public class MuslimPrayTime {
    public int hour;
    public int minute;
    public int id;
    public String name;

    @Override
    public String toString() {
        return "MuslimPrayTime{" +
                "hour=" + hour +
                ", minute=" + minute +
                ", id=" + id +
                '}';
    }
}
