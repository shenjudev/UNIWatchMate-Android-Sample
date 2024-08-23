package com.sjbt.sdk.sample.model.muslim;

public class MuslimTimeCalculateTypeParam {
    public static final int SHIA_ITHNA_ASHARI = 0;
    public static final int UNIVERSITY_OF_ISLAMIC_SCIENCE = 1;
    public static final int MUSLIM_WORLD_LEAGUE = 2;
    public static final int ISLAMIC_SOCIETY_OF_NORTH_AMERICA = 3;
    public static final int UMM_AL_QURA = 4;

    public static final int SHAFII = 0;
    public static final int HANAFI = 1;

    private int calculateType;
    private int juristicMethodType;

    public MuslimTimeCalculateTypeParam(int calculateType,int juristicMethodType) {
        this.calculateType = calculateType;
        this.juristicMethodType = juristicMethodType;
    }

    public int getCalculateType() {
        return calculateType;
    }

    public void setCalculateType(int calculateType) {
        this.calculateType = calculateType;
    }

    public int getJuristicMethodType() {
        return juristicMethodType;
    }

    public void setJuristicMethodType(int juristicMethodType) {
        this.juristicMethodType = juristicMethodType;
    }
}
