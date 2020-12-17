package com.yunio.videocapture.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.yunio.photoplugin.PhotoPluginModule;
import com.yunio.photoplugin.R;
import com.yunio.videocapture.entity.Folder.Media;
import com.yunio.videocapture.utils.Constant;
import com.yunio.videocapture.utils.PhotoUtils;

public class SelectAvatarActivity extends SelectImageBaseActivity {
    public static final String EXTRA_AVATAR_PATH = "avatar_path";
    private int mWidth, mQuality;

    public static Intent createLauncherIntent(Context context, int width, int quality) {
        Intent intent = new Intent(context, SelectAvatarActivity.class);
        Constant.addImageParams(intent, width, quality);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mWidth = Constant.getWidthFromIntent(intent);
        mQuality = Constant.getQualityFromIntent(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_select_avatar;
    }

    @Override
    protected void updateImageItem(View convertView, boolean isImage, int position,
                                   final Media media) {
        // super.updateImageItem(convertView, isImage, position, media);
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectSuccess(media.getPath());
            }
        });
    }

    private void selectSuccess(String path) {
        if (mWidth > 0 && mQuality > 0 && mQuality <= 100) {
            path = PhotoUtils.compress(path, false, mWidth, mQuality);
        }
        Intent dataIntent = new Intent();
        dataIntent.putExtra(EXTRA_AVATAR_PATH, path);
        //        setResult(RESULT_OK, dataIntent);
        PhotoPluginModule.onActivityResult(PhotoPluginModule.REQUEST_CODE_SELECT_AVATAR, RESULT_OK, dataIntent);
        finish();
    }

    @Override
    public boolean isSelectAvatar() {
        return true;
    }

    @Override
    public boolean isSupportGif() {
        return false;
    }
}
