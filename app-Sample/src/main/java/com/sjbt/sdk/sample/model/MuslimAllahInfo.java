package com.sjbt.sdk.sample.model;

public class MuslimAllahInfo {

    private int id;
    private String englishName;
    private String arabicName;
    private String indonesianName;
    private boolean favorite;


    public MuslimAllahInfo(int id, String englishName, String arabicName, String indonesianName, boolean favorite) {
        this.id = id;
        this.englishName = englishName;
        this.arabicName = arabicName;
        this.indonesianName = indonesianName;
        this.favorite = favorite;
    }

    public MuslimAllahInfo(int id, boolean favorite) {
        this.id = id;
        this.favorite = favorite;
    }

    public int getId() {
        return id;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getArabicName() {
        return arabicName;
    }

    public String getIndonesianName() {
        return indonesianName;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

}
