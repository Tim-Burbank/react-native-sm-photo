package com.yunio.videocapture.resource.entity;

import com.google.gson.annotations.SerializedName;

public class StringConfig implements IConfig {
    @SerializedName("press_start")
    private String pressStart;
    @SerializedName("move_up_cancel")
    private String moveUpCancel;
    @SerializedName("undo_cancel")
    private String undoCancel;
    @SerializedName("please_read_code")
    private String pleaseReadCode;
    private String preview;
    private String upload;
    private String camera;
    @SerializedName("all_photo")
    private String allPhoto;
    @SerializedName("total_x_photo")
    private String totalXPhoto;
    @SerializedName("send_image_max_count")
    private String sendImageMaxCount;
    @SerializedName("send_image_only_one")
    private String sendImageOnlyOne;
    @SerializedName("wait_compress")
    private String waitCompress;
    @SerializedName("no_permission_for_camera")
    private String noPermissionForCamera;
    @SerializedName("no_permission_for_record")
    private String noPermissionForRecord;
    @SerializedName("no_permission_for_record_2")
    private String noPermissionForRecord2;
    private String loading;
    @SerializedName("storage_error")
    private String storageError;
    @SerializedName("wait_compress_image")
    private String waitCompressImage;
    @SerializedName("need_permission_tips")
    private String needPermissionTips;
    @SerializedName("no_permission_all")
    private String noPermissionAll;
    @SerializedName("all_video_photo")
    private String allVideoPhoto;
    @SerializedName("all_video")
    private String allVideo;
    @SerializedName("wait_compress_image_video")
    private String waitCompressImageVideo;
    @SerializedName("text_dialog_title")
    private String textDialogTitle;
    @SerializedName("text_dialog_content")
    private String textDialogContent;
    private String cancel;
    private String Return;
    @SerializedName("video_dialog_title")
    private String videoDialogTitle;
    @SerializedName("video_dialog_seconds")
    private String videoDialogSeconds;
    @SerializedName("video_dialog_minutes")
    private String videoDialogMinutes;
    private String confirm;
    @SerializedName("bad_video")
    private String badVideo;

    public String getPressStart() {
        return pressStart;
    }

    public void setPressStart(String pressStart) {
        this.pressStart = pressStart;
    }

    public String getMoveUpCancel() {
        return moveUpCancel;
    }

    public void setMoveUpCancel(String moveUpCancel) {
        this.moveUpCancel = moveUpCancel;
    }

    public String getUndoCancel() {
        return undoCancel;
    }

    public void setUndoCancel(String undoCancel) {
        this.undoCancel = undoCancel;
    }

    public String getPleaseReadCode() {
        return pleaseReadCode;
    }

    public void setPleaseReadCode(String pleaseReadCode) {
        this.pleaseReadCode = pleaseReadCode;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public String getAllPhoto() {
        return allPhoto;
    }

    public void setAllPhoto(String allPhoto) {
        this.allPhoto = allPhoto;
    }

    public String getTotalXPhoto() {
        return totalXPhoto;
    }

    public void setTotalXPhoto(String totalXPhoto) {
        this.totalXPhoto = totalXPhoto;
    }

    public String getSendImageMaxCount() {
        return sendImageMaxCount;
    }

    public void setSendImageMaxCount(String sendImageMaxCount) {
        this.sendImageMaxCount = sendImageMaxCount;
    }

    public String getSendImageOnlyOne() {
        return sendImageOnlyOne;
    }

    public void setSendImageOnlyOne(String sendImageOnlyOne) {
        this.sendImageOnlyOne = sendImageOnlyOne;
    }

    public String getWaitCompress() {
        return waitCompress;
    }

    public void setWaitCompress(String waitCompress) {
        this.waitCompress = waitCompress;
    }

    public String getNoPermissionForCamera() {
        return noPermissionForCamera;
    }

    public void setNoPermissionForCamera(String noPermissionForCamera) {
        this.noPermissionForCamera = noPermissionForCamera;
    }

    public String getNoPermissionForRecord() {
        return noPermissionForRecord;
    }

    public void setNoPermissionForRecord(String noPermissionForRecord) {
        this.noPermissionForRecord = noPermissionForRecord;
    }

    public String getNoPermissionForRecord2() {
        return noPermissionForRecord2;
    }

    public void setNoPermissionForRecord2(String noPermissionForRecord2) {
        this.noPermissionForRecord2 = noPermissionForRecord2;
    }

    public String getLoading() {
        return loading;
    }

    public void setLoading(String loading) {
        this.loading = loading;
    }

    public String getStorageError() {
        return storageError;
    }

    public void setStorageError(String storageError) {
        this.storageError = storageError;
    }

    public String getWaitCompressImage() {
        return waitCompressImage;
    }

    public void setWaitCompressImage(String waitCompressImage) {
        this.waitCompressImage = waitCompressImage;
    }

    public String getNeedPermissionTips() {
        return needPermissionTips;
    }

    public void setNeedPermissionTips(String needPermissionTips) {
        this.needPermissionTips = needPermissionTips;
    }

    public String getNoPermissionAll() {
        return noPermissionAll;
    }

    public void setNoPermissionAll(String noPermissionAll) {
        this.noPermissionAll = noPermissionAll;
    }

    public String getAllVideoPhoto() {
        return allVideoPhoto;
    }

    public void setAllVideoPhoto(String allVideoPhoto) {
        this.allVideoPhoto = allVideoPhoto;
    }

    public String getAllVideo() {
        return allVideo;
    }

    public void setAllVideo(String allVideo) {
        this.allVideo = allVideo;
    }

    public String getWaitCompressImageVideo() {
        return waitCompressImageVideo;
    }

    public void setWaitCompressImageVideo(String waitCompressImageVideo) {
        this.waitCompressImageVideo = waitCompressImageVideo;
    }

    public String getTextDialogTitle() {
        return textDialogTitle;
    }

    public void setTextDialogTitle(String textDialogTitle) {
        this.textDialogTitle = textDialogTitle;
    }

    public String getTextDialogContent() {
        return textDialogContent;
    }

    public void setTextDialogContent(String textDialogContent) {
        this.textDialogContent = textDialogContent;
    }

    public String getCancel() {
        return cancel;
    }

    public void setCancel(String cancel) {
        this.cancel = cancel;
    }

    public String getReturn() {
        return Return;
    }

    public void setReturn(String aReturn) {
        Return = aReturn;
    }

    public String getVideoDialogTitle() {
        return videoDialogTitle;
    }

    public void setVideoDialogTitle(String videoDialogTitle) {
        this.videoDialogTitle = videoDialogTitle;
    }

    public String getVideoDialogSeconds() {
        return videoDialogSeconds;
    }

    public void setVideoDialogSeconds(String videoDialogSeconds) {
        this.videoDialogSeconds = videoDialogSeconds;
    }

    public String getVideoDialogMinutes() {
        return videoDialogMinutes;
    }

    public void setVideoDialogMinutes(String videoDialogMinutes) {
        this.videoDialogMinutes = videoDialogMinutes;
    }

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public String getBadVideo() {
        return badVideo;
    }

    public void setBadVideo(String badVideo) {
        this.badVideo = badVideo;
    }
}
