package com.sjbt.sdk.sample.entity;

public class MediaCountBean {
    public MediaCountBean(int photo_num, int video_num, int record_num, int music_num) {
        this.photo_num = photo_num;
        this.video_num = video_num;
        this.record_num = record_num;
        this.music_num = music_num;
    }

    @Override
    public String toString() {
        return
                "photo: " + photo_num +
                ", video: " + video_num +
                ", record: " + record_num +
                ", music: " + music_num ;
    }

    /**
     * 照片数量
     */
    public int photo_num = -1;
    /**
     * 视频数量
     */
    public int video_num = -1;
    /**
     * 录音数量
     */
    public int record_num = -1;
    /**
     * 音乐数量
     */
    public int music_num = -1;
}
