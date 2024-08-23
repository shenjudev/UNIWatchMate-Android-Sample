package com.sjbt.sdk.sample.model.muslim;

public class MuslimTimeCalculateTypeUI {
    public static final int SHIA_ITHNA_ASHARI = 0;
    public static final int UNIVERSITY_OF_ISLAMIC_SCIENCE = 1;
    public static final int MUSLIM_WORLD_LEAGUE = 2;
    public static final int ISLAMIC_SOCIETY_OF_NORTH_AMERICA = 3;
    public static final int UMM_AL_QURA = 4;

    public static final int SHAFII = 0;
    public static final int HANAFI = 1;

    private int type;
    private String name;

    public MuslimTimeCalculateTypeUI(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
