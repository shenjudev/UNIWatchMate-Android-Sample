package com.sjbt.sdk.entity.old;

import android.os.Parcel;
import android.os.Parcelable;

public class BatteryBean implements Parcelable {

    /**
     * is_charging : false
     * battery_main : 90
     * battery_r1 : -1
     * battery_r2 : -1
     */
    private int is_charging;
    private int battery_main;
    private int battery_r1;
    private int battery_r2;

    public int isIs_charging() {
        return is_charging;
    }

    public void setIs_charging(int is_charging) {
        this.is_charging = is_charging;
    }

    public int getBattery_main() {
        return battery_main;
    }

    public void setBattery_main(int battery_main) {
        this.battery_main = battery_main;
    }

    public int getBattery_r1() {
        return battery_r1;
    }

    public void setBattery_r1(int battery_r1) {
        this.battery_r1 = battery_r1;
    }

    public int getBattery_r2() {
        return battery_r2;
    }

    public void setBattery_r2(int battery_r2) {
        this.battery_r2 = battery_r2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.is_charging);
        dest.writeInt(this.battery_main);
        dest.writeInt(this.battery_r1);
        dest.writeInt(this.battery_r2);
    }

    public void readFromParcel(Parcel source) {
        this.is_charging = source.readInt();
        this.battery_main = source.readInt();
        this.battery_r1 = source.readInt();
        this.battery_r2 = source.readInt();
    }

    public BatteryBean() {
    }

    protected BatteryBean(Parcel in) {
        this.is_charging = in.readInt();
        this.battery_main = in.readInt();
        this.battery_r1 = in.readInt();
        this.battery_r2 = in.readInt();
    }

    public static final Creator<BatteryBean> CREATOR = new Creator<BatteryBean>() {
        @Override
        public BatteryBean createFromParcel(Parcel source) {
            return new BatteryBean(source);
        }

        @Override
        public BatteryBean[] newArray(int size) {
            return new BatteryBean[size];
        }
    };

    @Override
    public String toString() {
        return "BatteryBean{" +
                "is_charging=" + is_charging +
                ", battery_main=" + battery_main +
                ", battery_r1=" + battery_r1 +
                ", battery_r2=" + battery_r2 +
                '}';
    }
}
