package com.sjbt.sdk.sample.model.muslim;

public class MuslimCalcParam {
    public int calcType;
    public int juristicMethod; // 0:Shafii 1:Hanafi
    public int year;
    public int month;
    public int day;
    public int timeZone;
    public double latitude;
    public double longitude;

    public MuslimCalcParam() {
        calcType = 0;
        juristicMethod = 0;
        year = 0;
        month = 0;
        day = 0;
        latitude = 0;
        longitude = 0;
        timeZone = 0;
    }

    @Override
    public MuslimCalcParam clone(){
        MuslimCalcParam temp = null;
        try {
            temp = (MuslimCalcParam) super.clone();
        }catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return temp;
    }
}
