package com.yunio.videocapture.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.yunio.videocapture.utils.FileUtils;
import com.yunio.videocapture.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

public class Folder implements Comparator<Folder> {

    private final String TAG = "Folder";

    private String path;
    private String childPath;

    public void setChildList(ArrayList<Media> childList) {
        this.childList = childList;
    }

    private ArrayList<Media> childList;

    private long createDate;

    public ArrayList<Media> getChildList() {
        return childList;
    }

    public Folder(String path) {
        this.path = path;
    }

    public void setThumb(String path) {
        this.childPath = path;
    }

    public boolean contains(Media media) {
        if (media == null) {
            return false;
        }
        if (Utils.isEmpty(childList)) {
            return false;
        }
        return childList.contains(media);
    }

    public int indexOf(Media media) {
        if (media == null) {
            return -1;
        }
        return childList.indexOf(media);
    }

    public void set(int index, Media media) {
        childList.set(index, media);
    }

    public boolean add(String path, long createDate, long duration, boolean isVideo) {
        if (Utils.isEmpty(path)) {
            return false;
        }
        if (childList == null) {
            childList = new ArrayList<Media>();
        }
        if (Utils.isEmpty(childPath)) {
            childPath = path;
        }
        String suffix = FileUtils.getSuffix(path);
        Media media = new Media();
        media.setPath(path);
        media.setGif(suffix.equals("gif"));
        media.setCreateDate(createDate);
        if (isVideo) {
            media.setDuration(duration);
        }
        media.setVideo(isVideo);
        return childList.add(media);
    }

    public boolean add(LinkedList<Media> list) {
        if (Utils.isEmpty(list)) {
            return false;
        }
        if (childList == null) {
            childList = new ArrayList<Media>();
        }
        return childList.addAll(list);
    }

    public Media get(int position) {
        if (position >= getCount()) {
            return null;
        }
        return childList.get(position);
    }

    public String getName() {
        if (Utils.isEmpty(path)) {
            return null;
        }
        return FileUtils.getFileName(path);
    }

    public String getPath() {
        return path;
    }

    public String getChild() {
        return childPath;
    }

    public int getCount() {
        if (Utils.isEmpty(childList)) {
            return 0;
        }
        return childList.size();
    }

    public void init() {
        if (childList == null) {
            return;
        }
        for (Media media : childList) {
            media.checked = false;
        }
    }

    public boolean valid() {
        if (Utils.isEmpty(path) || Utils.isEmpty(childPath)) {
            return false;
        }
        try {
            File curFile = new File(path);
            if (curFile == null || curFile.isHidden() || curFile.isFile()) {
                return false;
            }
            File childFile = new File(childPath);
            if (childFile == null || childFile.isHidden() || childFile.isDirectory()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<String> getChecked() {
        ArrayList<String> list = new ArrayList<String>();
        for (Media media : childList) {
            if (media.isChecked()) {
                list.add(media.path);
            }
        }
        return list;
    }

    public void clearChecked() {
        for (Media media : childList) {
            if (media.isChecked()) {
                media.setChecked(false);
            }
        }
    }

    @Override
    public int compare(Folder lhs, Folder rhs) {
        try {
            return lhs.path.compareToIgnoreCase(rhs.path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Folder [TAG=" + TAG + ", path=" + path + ", childPath=" + childPath + ", childList="
                + childList + "]";
    }

    public static class Media implements Parcelable {
        private String path;
        private boolean checked;
        private long createDate;
        private boolean isVideo;
        private long duration;
        private String thumbnailPath;
        private boolean isGif;

        public Media(Parcel parcel) {
            path = parcel.readString();
            checked = parcel.readInt() == 1;
            isVideo = parcel.readInt() == 1;
            createDate = parcel.readLong();
            duration = parcel.readLong();
            thumbnailPath = parcel.readString();
            isGif = parcel.readInt() == 1;
        }

        public Media() {

        }

        public boolean isGif() {
            return isGif;
        }

        public void setGif(boolean gif) {
            isGif = gif;
        }

        public String getThumbnailPath() {
            return thumbnailPath;
        }

        public void setThumbnailPath(String thumbnailPath) {
            this.thumbnailPath = thumbnailPath;
        }

        public void setVideo(boolean video) {
            isVideo = video;
        }

        public boolean isVideo() {
            return isVideo;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public long getCreateDate() {
            return createDate;
        }

        public void setCreateDate(long createDate) {
            this.createDate = createDate;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public void check() {
            if (TextUtils.isEmpty(path)) {
                return;
            }
            this.checked = !checked;
        }


        @Override
        public boolean equals(Object o) {
            if (o instanceof Media) {
                return path.equals(((Media) o).getPath());
            }
            return super.equals(o);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public final static Creator<Media> CREATOR = new Creator<Media>() {

            @Override
            public Media[] newArray(int size) {
                return new Media[size];
            }

            @Override
            public Media createFromParcel(Parcel source) {
                return new Media(source);
            }
        };

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(path);
            dest.writeInt(checked ? 1 : 0);
            dest.writeInt(isVideo ? 1 : 0);
            dest.writeLong(createDate);
            dest.writeLong(duration);
            dest.writeString(thumbnailPath);
            dest.writeInt(isGif ? 1 : 0);
        }

    }

}
