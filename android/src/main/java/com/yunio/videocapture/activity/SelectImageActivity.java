package com.yunio.videocapture.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yunio.photoplugin.PhotoPluginModule;
import com.yunio.photoplugin.R;
import com.yunio.videocapture.entity.Folder;
import com.yunio.videocapture.entity.Folder.Media;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.ResourceUtils;
import com.yunio.videocapture.resource.entity.StringConfig;
import com.yunio.videocapture.utils.Constant;
import com.yunio.videocapture.utils.FileUtils;
import com.yunio.videocapture.utils.LogUtils;
import com.yunio.videocapture.utils.ToastUtils;
import com.yunio.videocapture.utils.Utils;
import com.yunio.videocapture.utils.VideoUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SelectImageActivity extends SelectImageBaseActivity {
  private static final String TAG = "SelectImageActivity";
  public static final int DEFAULT_MAX_COUNT = 3;
  public static final String EXTRA_IMAGE_PATHS = "image_paths";
  public static final String EXTRA_VIDEOS = "videos";
  public static final String EXTRA_MIN_COUNT = "min_count";
  public static final String EXTRA_MAX_COUNT = "max_count";
  public static final String EXTRA_WIDTH = "width";
  public static final String EXTRA_QUALITY = "quality";
  private final static String EXTRA_DURATION = "duration";
  public final static String EXTRA_VIDEO_QUALITY = "video_quality";
  private final static String EXTRA_SELECT_IMAGE_VIDEO = "isCanSelectImageAndVideo";
  private final static String EXTRA_ONLY_SELECT_IMAGE = "isOnlySelectImage";
  private final static String EXTRA_REQUEST_CODE = "request_code";
  private final static String EXTRA_SHOULD_COMPRESS_VIDEO = "shouldCompressVideo";
  private TextView mTvCount, mTvPreview;
  private View mSendView;
  private int mCheckedCount;
  private int mMinCount;
  public int mMaxCount = 3;
  private int mWidth, mQuality;
  private int mVideoQuality;
  private ArrayList<Media> mSelectedMedias = new ArrayList<Media>();
  protected boolean isSelectImage;//是否已经选择过图片
  protected boolean isSelectVideo;//是否已经选择过视频
  private boolean isCanSelectImageAndVideo = false;//是否图片和视频都可以选
  private boolean shouldCompressVideo = true;
  private int maxDuration;
  private boolean isOnlySelectImage;//是否是只选择图片模式
  int number = 0;
  private TextView mTvSendView;

  private int requestCode;
  private AlertDialog.Builder dialog;

  private GradientDrawable mThemeDrawable;

  public static Intent createLauncherIntent(Context context, int minCount, int maxCount, int width,
                                            int quality) {
    Intent intent = new Intent(context, SelectImageActivity.class);
    fillImageIntentData(intent, maxCount, width, quality, minCount);
    return intent;
  }

  public static void fillImageIntentData(Intent intent, int maxCount, int width, int quality, int minCount) {
    if (maxCount > 0) {
      intent.putExtra(EXTRA_MAX_COUNT, maxCount);
    }
    intent.putExtra(EXTRA_MIN_COUNT, minCount);
    intent.putExtra(EXTRA_REQUEST_CODE, PhotoPluginModule.REQUEST_CODE_SELECT_IMAGE);
    intent.putExtra(EXTRA_ONLY_SELECT_IMAGE, true);
    Constant.addImageParams(intent, width, quality);
  }

  /**
   * 图片视频混选
   *
   * @param context
   * @param minCount
   * @param maxCount
   * @param width
   * @param picQuality               图片质量
   * @param videoQuality             视频质量
   * @param maxDuration              //视频最大时长
   * @param isCanSelectImageAndVideo //是否图片和视频都可以同时选
   * @return
   */
  public static Intent createLauncherIntent(Context context, int minCount, int maxCount, int width,
                                            int picQuality, int videoQuality, int maxDuration, boolean isCanSelectImageAndVideo, boolean shouldCompressVideo) {
    Intent intent = new Intent(context, SelectImageActivity.class);
    if (maxCount > 0) {
      intent.putExtra(EXTRA_MAX_COUNT, maxCount);
    }
    intent.putExtra(EXTRA_MIN_COUNT, minCount);
    intent.putExtra(EXTRA_REQUEST_CODE, PhotoPluginModule.REQUEST_CODE_SELECT_VIDEO_IMAGE);
    Constant.addImageParams(intent, width, picQuality);
    intent.putExtra(EXTRA_DURATION, maxDuration);
    intent.putExtra(EXTRA_VIDEO_QUALITY, videoQuality);
    intent.putExtra(EXTRA_SELECT_IMAGE_VIDEO, isCanSelectImageAndVideo);
    intent.putExtra(EXTRA_SHOULD_COMPRESS_VIDEO, shouldCompressVideo);
    return intent;
  }

  @Override
  protected void onInitUI() {
    isOnlySelectImage = getIntent().getBooleanExtra(EXTRA_ONLY_SELECT_IMAGE, false);
    requestCode = getIntent().getIntExtra(EXTRA_REQUEST_CODE, PhotoPluginModule.REQUEST_CODE_SELECT_IMAGE);
    super.onInitUI();
    Intent intent = getIntent();
    mMaxCount = intent.getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
    mMinCount = intent.getIntExtra(EXTRA_MIN_COUNT, 0);
    maxDuration = intent.getIntExtra(EXTRA_DURATION, 0);
    isCanSelectImageAndVideo = intent.getBooleanExtra(EXTRA_SELECT_IMAGE_VIDEO, false);
    shouldCompressVideo = intent.getBooleanExtra(EXTRA_SHOULD_COMPRESS_VIDEO, true);
    mWidth = Constant.getWidthFromIntent(intent);
    mQuality = Constant.getQualityFromIntent(intent);
    mVideoQuality = intent.getIntExtra(EXTRA_VIDEO_QUALITY, 23);
    mTvCount = (TextView) findViewById(R.id.tv_count);
    mTvPreview = (TextView) findViewById(R.id.tv_preview);
    mSendView = findViewById(R.id.layout_send);
    mTvSendView = findViewById(R.id.tv_send);
    mTvPreview.setOnClickListener(this);
    mSendView.setOnClickListener(this);
    mCheckedCount = mSelectedMedias.size();
    setCheckedCount();
    LogUtils.d(TAG, "onInitUI maxCount: " + mMaxCount);
    LogUtils.d(TAG, "onInitUI duration: " + maxDuration);
    LogUtils.d(TAG, "onInitUI mMinCount " + mMinCount);
    mThemeDrawable = ResourceUtils.getThemeDrawable();
    Drawable count = mTvCount.getBackground();
    if (count != null) {
      count.setColorFilter(ResourceUtils.getThemeColor(), PorterDuff.Mode.SRC_IN);
    }
    StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
    if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getUpload())) {
      mTvSendView.setText(stringConfig.getUpload());
    }
  }

  @Override
  public boolean isSelectAvatar() {
    return isOnlySelectImage;
  }


  @Override
  protected void updateImageItem(View convertView, boolean isImage, final int position,
                                 final Media media) {
    final MyViewHolde holder = (MyViewHolde) convertView.getTag();
    //        ViewUtils.setVisibility(holder.mCtvAvatar, View.VISIBLE);
    // holder.mCtvAvatar.setChecked(media.isChecked());
    //        ViewUtils.setVisibility(holder.mCtvAvatar, media.isChecked() ? View.VISIBLE : View.GONE);
    if (media.isVideo()) {
      holder.mDuration.setText(Utils.resetDuration(Utils.getRoundedFloat(media.getDuration() / 1000f)));
      holder.mDuration.setVisibility(View.VISIBLE);
      holder.mIvVideoIc.setVisibility(View.VISIBLE);
    } else {
      holder.mDuration.setVisibility(View.GONE);
      holder.mIvVideoIc.setVisibility(View.GONE);
      if (media.isGif()) {
        holder.mDuration.setVisibility(View.VISIBLE);
        holder.mDuration.setText("GIF");
      }
    }
    int indexOf = mSelectedMedias.indexOf(media);
    if (indexOf >= 0) {
      holder.mTvSelectNumber.setText("" + (indexOf + 1));
      //            holder.mTvSelectNumber.setBackgroundResource(R.drawable.bubble_point);
      holder.mTvSelectNumber.setBackground(mThemeDrawable);
    } else {
      holder.mTvSelectNumber.setText("");
      holder.mTvSelectNumber.setBackgroundResource(R.drawable.middle_transparent_point);
    }
    if (!isCanSelectImageAndVideo) {
      if (isSelectVideo && !isSelectImage) {
        if (mSelectedMedias.contains(media)) {
          holder.mTvUnselect.setVisibility(View.GONE);
        } else {
          holder.mTvUnselect.setVisibility(View.VISIBLE);
        }
      } else if (!isSelectVideo && isSelectImage) {
        if (media.isVideo()) {
          holder.mTvUnselect.setVisibility(View.VISIBLE);
        } else {
          holder.mTvUnselect.setVisibility(View.GONE);
        }

      } else if (!isSelectVideo && !isSelectImage) {
        holder.mTvUnselect.setVisibility(View.GONE);
      }
    }

    convertView.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (!isCanSelectImageAndVideo) {
          if (isSelectVideo && !isSelectImage) {
            if (media.isVideo() && mSelectedMedias.contains(media)) {//取消选择之前视频
              onImageCheckedClicked(holder, media);
            }
            return;
          } else if (!media.isVideo() && !isSelectVideo && isSelectImage) {
            onImageCheckedClicked(holder, media);
          } else if (!isSelectImage && !isSelectVideo) {
            onImageCheckedClicked(holder, media);
          }
        } else {
          onImageCheckedClicked(holder, media);
        }

        // getLocalFragmentManager().replaceFragmentForResult(REQUEST_CODE_PREVIEW,
        // MediaSelectScaleImageFragment.newInstance(position,
        // mFolder.getChildList(),
        // mSelectedMedias));
      }
    });
  }

  @Override
  protected void loadFolder(Folder folder) {
    for (Media media : mSelectedMedias) {
      if (folder.contains(media)) {
        folder.set(folder.indexOf(media), media);
      }
    }
    super.loadFolder(folder);
  }

  private void onImageCheckedClicked(ViewHolder holder, Media media) {
    if (media.isVideo()) {
      isSelectVideo = true;
      isSelectImage = false;
    } else {//只能选图片
      isSelectVideo = false;
      isSelectImage = true;
    }
    if (!media.isChecked()) {
      if (mCheckedCount >= mMaxCount) {
        StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
        String maxCount = getString(R.string.send_image_max_count, mMaxCount);
        if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getSendImageMaxCount())) {
          maxCount = String.format(stringConfig.getSendImageMaxCount(), mMaxCount);
        }
        String onlyOne = getString(R.string.send_image_only_one);
        if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getSendImageOnlyOne())) {
          onlyOne = stringConfig.getSendImageOnlyOne();
        }
        ToastUtils.showToast(mMaxCount > 1 ? maxCount : onlyOne);
        return;
      }
      if (PhotoPluginModule.GifSizeLimit > 0 && media.isGif()) {
        double gifLimitSize = PhotoPluginModule.GifSizeLimit * 1024 * 1024;
        long mediaSize = FileUtils.size(media.getPath());
        if (mediaSize >= gifLimitSize) {
          String limit = PhotoPluginModule.GifSizeLimit + "MB";
          if (PhotoPluginModule.GifSizeLimit < 1) {
            DecimalFormat format = new DecimalFormat("#");
            limit = format.format(PhotoPluginModule.GifSizeLimit * 1000) + "KB";
          }
          ToastUtils.showToast(getString(R.string.gif_error_tips, limit));
          return;
        }

      }
    }
    media.setChecked(!media.isChecked());
    // holder.mCtvAvatar.toggle();
    //        ViewUtils.setVisibility(holder.mCtvAvatar, media.isChecked() ? View.VISIBLE : View.GONE);
    updateCheckedList(media);
    mCheckedCount = media.isChecked() ? mCheckedCount + 1 : mCheckedCount - 1;
    setCheckedCount();
    if (Utils.isEmpty(mSelectedMedias)) {
      isSelectImage = false;
      isSelectVideo = false;
    }
    handleImageAdapter();
  }

  private void updateCheckedList(Media media) {
    if (media.isChecked()) {
      mSelectedMedias.add(media);
    } else {
      if (mSelectedMedias.contains(media)) {
        mSelectedMedias.remove(media);
      }
    }
  }

  private void setCheckedCount() {
    if (mCheckedCount > 0) {
      mTvCount.setVisibility(View.VISIBLE);
      mTvCount.setText(String.valueOf(mCheckedCount));
    } else {
      mTvCount.setVisibility(View.GONE);
    }
    mTvSendView.setTextColor(mCheckedCount > 0 ? ResourceUtils.getThemeColor() : ResourceUtils.getAlphaThemeColor());
    mTvPreview.setClickable(mCheckedCount > 0);
    mSendView.setClickable(mCheckedCount > 0);
  }

  @Override
  public void onClick(View v) {
    super.onClick(v);
    int id = v.getId();
    if (id == R.id.tv_preview) {
      ArrayList<Media> medias = new ArrayList<Media>();
      medias.addAll(mSelectedMedias);
      SelectImageScaleImageActivity.startActivity(this, medias, mSelectedMedias, mMaxCount);
    } else if (id == R.id.layout_send) {

      if (mMinCount > mCheckedCount && isSelectImage) {
        String minContent = getString(R.string.send_image_min_count, mMinCount);
        ToastUtils.showToast(minContent);
        return;
      }
      //            selectSuccess();
      showVideoToolongTip();

    }

  }

  private class MyViewHolde extends ViewHolder {
    TextView mDuration;
    TextView mTvUnselect;
    TextView mTvSelectNumber;
    ImageView mIvVideoIc;

    public MyViewHolde(View convertView) {
      super(convertView);
    }

    @Override
    public void initView(View convertView) {
      super.initView(convertView);
      mDuration = (TextView) convertView.findViewById(R.id.tv_duration);
      mTvUnselect = (TextView) convertView.findViewById(R.id.tv_unselect);
      mTvSelectNumber = (TextView) convertView.findViewById(R.id.tv_select_number);
      mIvVideoIc = (ImageView) convertView.findViewById(R.id.iv_video);
      RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mImageSize, mImageSize);
      mCtvAvatar.setLayoutParams(lp);
      mTvUnselect.setLayoutParams(lp);
    }
  }

  @Override
  public ViewHolder getViewHolder(View convertView) {
    return new MyViewHolde(convertView);
  }

  @Override
  public int getItemResId() {
    return R.layout.adapter_video_image;
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.activity_select_image;
  }

  public void showVideoToolongTip() {
    if (mSelectedMedias.isEmpty()) {
      return;
    }
    boolean isTooLong = false;
    String content = "";
    for (Media media : mSelectedMedias) {
      if (media.isVideo()) {
        LogUtils.e(TAG, "maxDuration" + maxDuration * 1000 + " media duration" + media.getDuration());
        if (media.getDuration() < maxDuration * 1000) {
          break;
        }
        isTooLong = true;
        StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
        if (maxDuration % 60 == 0) {//是否是一分钟整数倍
          long minutes = maxDuration / 60;//分
          content = getString(R.string.video_dialog_minutes, minutes);
          if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getVideoDialogMinutes())) {
            content = String.format(stringConfig.getVideoDialogMinutes(), minutes);
          }
          break;
        } else {
          content = getString(R.string.video_dialog_seconds, maxDuration);
          if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getVideoDialogSeconds())) {
            content = String.format(stringConfig.getVideoDialogSeconds(), maxDuration);
          }
          break;
        }
      }
    }
    if (isTooLong) {
      if (dialog == null) {
        StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
        String video_dialog_title = getString(R.string.video_dialog_title);
        if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getVideoDialogTitle())) {
          video_dialog_title = stringConfig.getVideoDialogTitle();
        }
        String cancel = getString(R.string.cancel);
        if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getCancel())) {
          cancel = stringConfig.getCancel();
        }
        String confirm = getString(R.string.confirm);
        if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getConfirm())) {
          confirm = stringConfig.getConfirm();
        }
        dialog = new AlertDialog.Builder(this);
        dialog.setTitle(video_dialog_title).setMessage(content);
        dialog.setNegativeButton(cancel, null);
        dialog.setPositiveButton(confirm, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            selectSuccess();
          }
        });
      }
      dialog.show();
    } else {
      selectSuccess();
    }
  }

  private void selectSuccess() {
    if (mSelectedMedias.isEmpty()) {
      return;
    }
    VideoUtils.performMediasSelectedComplete(this, requestCode, mSelectedMedias,
      maxDuration, mWidth, mQuality, mVideoQuality, shouldCompressVideo);
    //        ProgressDialogUtils.showProgressDialog(this,
    //                R.string.wait_compress_image_video);
    //        ThreadPoolManager.getDefaultThreadPool().add(new Runnable() {
    //
    //            @Override
    //            public void run() {
    //                final ArrayList<String> images = new ArrayList<String>();
    //                final ArrayList<Media> videos = new ArrayList<Media>();
    //                for (int i = 0; i < mSelectedMedias.size(); i++) {
    //                    // paths[i] = mSelectedMedias.get(i).getPath();
    //                    Media media = mSelectedMedias.get(i);
    //                    if (media.isVideo()) {
    //                        Media video = new Media();
    //                        String videoPath = FileUtils.compressVideo(media, duration);
    //                        String thumbNailPath = FileUtils.saveThumbnail(media);
    //                        video.setPath(videoPath);
    //                        video.setThumbnailPath(thumbNailPath);
    //                        video.setDuration(media.getDuration());
    //                        videos.add(video);
    //                    } else {
    //                        final boolean requestCompress = mWidth > 0 && mQuality > 0 && mQuality <= 100;
    //                        if (requestCompress) {
    //                            images.add(PhotoUtils.compress(mSelectedMedias.get(i).getPath(), false,
    //                                    mWidth, mQuality));
    //                        } else {
    //                            images.add(mSelectedMedias.get(i).getPath());
    //                        }
    //                    }
    //                }
    //                runOnUiThread(new Runnable() {
    //                    public void run() {
    //                        ProgressDialogUtils.dismissProgressDialog();
    //                        Intent dataIntent = new Intent();
    //                        dataIntent.putExtra(EXTRA_IMAGE_PATHS, images);
    //                        dataIntent.putExtra(EXTRA_VIDEOS, videos);
    //                        setResult(Activity.RESULT_OK, dataIntent);
    //                        finish();
    //                    }
    //                });
    //            }
    //        });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      if (requestCode == SelectImageScaleImageActivity.REQUEST_CODE) {
        mSelectedMedias = data.getParcelableArrayListExtra(
          SelectImageScaleImageActivity.EXTRA_SELECTED_MEDIAS);
        mCheckedCount = mSelectedMedias.size();
        setCheckedCount();
        mFolder.clearChecked();
        for (Media media : mSelectedMedias) {
          if (mFolder.contains(media)) {
            mFolder.set(mFolder.indexOf(media), media);
          }
        }
        handleImageAdapter();
        boolean result = data.getBooleanExtra(SelectImageScaleImageActivity.EXTRA_RESULT,
          false);
        if (result) {
          //                    selectSuccess();
          showVideoToolongTip();
        }
      }
    }
  }

}
