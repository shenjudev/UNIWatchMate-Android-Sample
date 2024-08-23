package com.sjbt.sdk.sample.ui.fileTrans;

import android.graphics.drawable.Drawable;
/**
 * 音乐
 */
public class LocalFileBean {
	private Long Id;
	private String mediaId;
	private String fileName;
	private String title;
	private int duration;
	private String singer;
	private String album;
	private Long albumId;
	private String year;
	private String type;
	private String size;
	private String fileUrl;
	private boolean isSent;
	private Drawable albumBitmap;
	private boolean isSelected;

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	public String getFileName() {
		return fileName;
	}
 
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
 
	public String getTitle() {
		return title;
	}
 
	public void setTitle(String title) {
		this.title = title;
	}
 
	public int getDuration() {
		return duration;
	}
 
	public void setDuration(int duration) {
		this.duration = duration;
	}
 
	public String getSinger() {
		return singer;
	}
 
	public void setSinger(String singer) {
		this.singer = singer;
	}
 
	public String getAlbum() {
		return album;
	}
 
	public void setAlbum(String album) {
		this.album = album;
	}
 
	public String getYear() {
		return year;
	}
 
	public void setYear(String year) {
		this.year = year;
	}
 
	public String getType() {
		return type;
	}
 
	public void setType(String type) {
		this.type = type;
	}
 
	public String getSize() {
		return size;
	}
 
	public void setSize(String size) {
		this.size = size;
	}
 
	public String getFileUrl() {
		return fileUrl;
	}
 
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public boolean getIsSent() {
		return this.isSent;
	}

	public void setIsSent(boolean isSent) {
		this.isSent = isSent;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}

	public Drawable getAlbumBitmap() {
		return albumBitmap;
	}

	public void setAlbumBitmap(Drawable albumBitmap) {
		this.albumBitmap = albumBitmap;
	}

	public LocalFileBean() {
		super();
	}

	public LocalFileBean(Long Id, String mediaId, String fileName, String title,
                         int duration, String singer, String album, Long albumId, String year,
                         String type, String size, String fileUrl, boolean isSent) {
		this.Id = Id;
		this.mediaId = mediaId;
		this.fileName = fileName;
		this.title = title;
		this.duration = duration;
		this.singer = singer;
		this.album = album;
		this.albumId = albumId;
		this.year = year;
		this.type = type;
		this.size = size;
		this.fileUrl = fileUrl;
		this.isSent = isSent;
	}

	@Override
	public String toString() {
		return "LocalFileBean{" +
				"Id=" + Id +
				", fileName='" + fileName + '\'' +
				", title='" + title + '\'' +
				", duration=" + duration +
				", singer='" + singer + '\'' +
				", album='" + album + '\'' +
				", year='" + year + '\'' +
				", type='" + type + '\'' +
				", size='" + size + '\'' +
				", fileUrl='" + fileUrl + '\'' +
				", isSent=" + isSent +
				", albumBitmap='" + albumBitmap + '\'' +
				", isSelected=" + isSelected +
				'}';
	}

	public boolean getIsSelected() {
		return this.isSelected;
	}

	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public String getMediaId() {
		return this.mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public Long getAlbumId() {
		return this.albumId;
	}

	public void setAlbumId(Long albumId) {
		this.albumId = albumId;
	}



}