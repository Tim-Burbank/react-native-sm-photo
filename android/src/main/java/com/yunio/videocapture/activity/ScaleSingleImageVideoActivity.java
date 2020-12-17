package com.yunio.videocapture.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yunio.photoplugin.R;
import com.yunio.videocapture.entity.Folder;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.entity.StringConfig;
import com.yunio.videocapture.utils.FileUtils;
import com.yunio.videocapture.utils.UIUtils;
import com.yunio.videocapture.utils.WindowUtils;
import com.yunio.videocapture.view.PhotoView;
import com.yunio.videocapture.view.VideoPlayerView;


public class ScaleSingleImageVideoActivity extends BaseActivity implements View.OnClickListener {
  public final static String EXTRA_MEDIA = "media";
  private ViewStub mVbVideo;
  private ViewStub mVbImage;
  public final static String EXTRA_RESULT = "result";
  public final static int REQUEST_CODE = 1000;
  private Folder.Media media;
  private AlertDialog.Builder dialog;

  public static void startActivity(Activity activity, Folder.Media media) {
    Intent intent = new Intent(activity, ScaleSingleImageVideoActivity.class);
    intent.putExtra(EXTRA_MEDIA, media);
    activity.startActivityForResult(intent, REQUEST_CODE);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    WindowUtils.hideStatusBarShowNavBar(this, 0x33000000, 0x33000000);
    setContentView(getLayoutResId());
    media = getIntent().getParcelableExtra(EXTRA_MEDIA);
    initUI();
  }

  protected void initUI() {
    mVbVideo = (ViewStub) findViewById(R.id.vb_video);
    mVbImage = (ViewStub) findViewById(R.id.vb_image);
    View titleBar = findViewById(R.id.title_bar);
    findViewById(R.id.title_left_img).setOnClickListener(this);
    TextView tvComplete = findViewById(R.id.tv_complete);
    tvComplete.setOnClickListener(this);
//    View contentView = this.findViewById(android.R.id.content);
    titleBar.setPadding(0, WindowUtils.getStatusBarHeight(this), 0, 0);
    titleBar.setBackgroundColor(getResources().getColor(media.isVideo() ? R.color.transparent : R.color
      .black_alph_80));
    if (media.isVideo()) {
      showVideo();
    } else {
      showImage();
    }
    TextView titleMiddle = findViewById(R.id.title_middle_text);
    StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
    if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getPreview())) {
      titleMiddle.setText(stringConfig.getPreview());
    }
    if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getUpload())) {
      tvComplete.setText(stringConfig.getUpload());
    }
  }

  public void showVideo() {
    VideoPlayerView videoPlayerView = (VideoPlayerView) mVbVideo.inflate();
    videoPlayerView.setVideoPath(media.getPath());
    videoPlayerView.startPlay();
  }

  public void showImage() {
    View mImageLayout = mVbImage.inflate();
    PhotoView imageViewTouch = (PhotoView) mImageLayout.findViewById(R.id.iv_avater);
    ProgressBar progressBar = (ProgressBar) mImageLayout.findViewById(R.id.pb_progress);
    imageViewTouch.setProgress(progressBar);
    imageViewTouch.setImagePath(media.getPath(), UIUtils.getWidthPixels(), UIUtils.getHeightPixels() - UIUtils.getStatusBarHeight(this));
  }

  protected int getLayoutResId() {
    return R.layout.activity_scale_image_video;
  }

  @Override
  public void onClick(View view) {
    int id = view.getId();
    if (id == R.id.title_left_img) {
      confirmBack();
    } else if (id == R.id.tv_complete) {
      onBackResult(true);
    }

  }

  @Override
  public void onBackPressed() {
    confirmBack();
  }

  private void confirmBack() {
    if (dialog == null) {
      StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
      String text_dialog_title = getString(R.string.text_dialog_title);
      if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getTextDialogTitle())) {
        text_dialog_title = stringConfig.getTextDialogTitle();
      }
      String text_dialog_content = getString(R.string.text_dialog_content);
      if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getTextDialogContent())) {
        text_dialog_content = stringConfig.getTextDialogContent();
      }
      String cancel = getString(R.string.cancel);
      if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getCancel())) {
        cancel = stringConfig.getCancel();
      }
      String Return = getString(R.string.Return);
      if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getReturn())) {
        Return = stringConfig.getReturn();
      }
      dialog = new AlertDialog.Builder(this);
      dialog.setTitle(text_dialog_title).setMessage(text_dialog_content);
      dialog.setNegativeButton(cancel, null);
      dialog.setPositiveButton(Return, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          FileUtils.delete(media.getPath());
          onBackResult(false);
        }
      });
    }
    dialog.show();
  }

  private void onBackResult(boolean success) {
    Intent intent = new Intent();
    intent.putExtra(EXTRA_MEDIA, media);
    intent.putExtra(EXTRA_RESULT, success);
    setResult(RESULT_OK, intent);
    finish();
  }
}
