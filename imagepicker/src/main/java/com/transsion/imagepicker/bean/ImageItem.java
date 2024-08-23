package com.transsion.imagepicker.bean;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：图片信息
 * 修订历史：
 * ================================================
 */
public class ImageItem implements Serializable, Parcelable {
    public String name;       //图片的名字
    public String path;       //图片的路径
    public long size;         //图片的大小
    public int width;         //图片的宽度
    public int height;        //图片的高度
    public long duration;      //视频时长
    public boolean isVideo = false;   //是否是视频
    public String mimeType;   //图片的类型
    public long addTime;      //图片的创建时间
    public Uri mUri;


    /** 图片的路径和创建时间相同就认为是同一张图片 */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ImageItem) {
            ImageItem item = (ImageItem) o;
            return this.path.equalsIgnoreCase(item.path) && this.addTime == item.addTime;
        }

        return super.equals(o);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeLong(this.size);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeLong(this.duration);
        dest.writeByte(this.isVideo ? (byte) 1 : (byte) 0);
        dest.writeString(this.mimeType);
        dest.writeLong(this.addTime);
        dest.writeParcelable(this.mUri, flags);
    }

    public void readFromParcel(Parcel source) {
        this.name = source.readString();
        this.path = source.readString();
        this.size = source.readLong();
        this.width = source.readInt();
        this.height = source.readInt();
        this.duration = source.readLong();
        this.isVideo = source.readByte() != 0;
        this.mimeType = source.readString();
        this.addTime = source.readLong();
        this.mUri = source.readParcelable(Uri.class.getClassLoader());
    }

    public ImageItem() {
    }

    protected ImageItem(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
        this.size = in.readLong();
        this.width = in.readInt();
        this.height = in.readInt();
        this.duration = in.readLong();
        this.isVideo = in.readByte() != 0;
        this.mimeType = in.readString();
        this.addTime = in.readLong();
        this.mUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel source) {
            return new ImageItem(source);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };

    @Override
    public String toString() {
        return "ImageItem{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", width=" + width +
                ", height=" + height +
                ", duration=" + duration +
                ", isVideo=" + isVideo +
                ", mimeType='" + mimeType + '\'' +
                ", addTime=" + addTime +
                ", mUri=" + mUri +
                '}';
    }
}
