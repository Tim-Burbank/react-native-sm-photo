package com.yunio.videocapture.resource.entity;

import com.google.gson.annotations.SerializedName;

public class ImageConfig  implements IConfig{
    @SerializedName("selected_image")
    private String selectedImage;
    @SerializedName("unselected_image")
    private String unselectedImage;

    public String getSelectedImage() {
        return selectedImage;
    }

    public void setSelectedImage(String selectedImage) {
        this.selectedImage = selectedImage;
    }

    public String getUnselectedImage() {
        return unselectedImage;
    }

    public void setUnselectedImage(String unselectedImage) {
        this.unselectedImage = unselectedImage;
    }
}
