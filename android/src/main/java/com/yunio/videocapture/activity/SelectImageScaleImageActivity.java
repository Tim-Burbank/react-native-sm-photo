package com.yunio.videocapture.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import androidx.viewpager.widget.ViewPager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeControllerBuilder;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.views.image.ReactImageView;
import com.yunio.photoplugin.R;
import com.yunio.videocapture.entity.Folder.Media;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.ResourceUtils;
import com.yunio.videocapture.resource.entity.ImageConfig;
import com.yunio.videocapture.resource.entity.StringConfig;
import com.yunio.videocapture.utils.ToastUtils;
import com.yunio.videocapture.utils.UIUtils;
import com.yunio.videocapture.utils.WindowUtils;
import com.yunio.videocapture.view.PhotoView;
import com.yunio.videocapture.view.VideoPlayerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class SelectImageScaleImageActivity extends ScaleImageBaseActivity<Media>
  implements OnClickListener {
  public final static String EXTRA_SELECTED_MEDIAS = "selected_medias";
  public final static String EXTRA_RESULT = "result";
  public final static int REQUEST_CODE = 1000;
  private TextView mTvCount;
  private View mSendView;
  private ReactImageView mTvCheck;

  private ArrayList<Media> mSelectedMedias;
  private int mCheckedCount;
  private int mMaxCount;

  public static void startActivity(Activity activity, ArrayList<Media> medias,
                                   ArrayList<Media> selectedMedias, int maxCount) {
    Intent intent = new Intent(activity, SelectImageScaleImageActivity.class);
    intent.putExtra(EXTRA_MEDIAS, medias);
    intent.putExtra(EXTRA_SELECTED_MEDIAS, selectedMedias);
    intent.putExtra(SelectImageActivity.EXTRA_MAX_COUNT, maxCount);
    activity.startActivityForResult(intent, REQUEST_CODE);
  }

  @Override
  protected void initAdapter() {
    super.initAdapter();
    onPageSelected(position);
  }

  @Override
  protected void onInitUI() {
    mSelectedMedias = getIntent().getParcelableArrayListExtra(EXTRA_SELECTED_MEDIAS);
    mMaxCount = getIntent().getIntExtra(SelectImageActivity.EXTRA_MAX_COUNT,
      SelectImageActivity.DEFAULT_MAX_COUNT);
    mTvCount = (TextView) findViewById(R.id.tv_count);
    WindowUtils.isNavigationBarExist(this, new WindowUtils.OnNavigationStateListener() {
      @Override
      public void onNavigationState(boolean isShowing, int height) {
        RelativeLayout layoutBottom = findViewById(R.id.layout_bottom);
        layoutBottom.setPadding(UIUtils.dip2px(10), 0, UIUtils.dip2px(10), height);
      }
    });

    Drawable count = mTvCount.getBackground();
    if (count != null) {
      count.setColorFilter(ResourceUtils.getThemeColor(), PorterDuff.Mode.SRC_IN);
    }
    mSendView = findViewById(R.id.layout_send);
    TextView tvSubmit = findViewById(R.id.tv_submit);
    tvSubmit.setTextColor(ResourceUtils.getThemeColor());
    StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
    if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getUpload())) {
      tvSubmit.setText(stringConfig.getUpload());
    }

    findViewById(R.id.title_left_img).setOnClickListener(this);
    ResourceUtils.initTitleBack(getWindow().getDecorView());
    mSendView.setOnClickListener(this);
    mTvCheck = new ReactImageView(this, getDraweeControllerBuilder(), null, this);
    FrameLayout rootChecked = (FrameLayout) findViewById(R.id.layout_check);
    //        ViewGroup.LayoutParams lp = new FrameLayout.LayoutParams(50, 50);
    //        mTvCheck.setLayoutParams(lp);
    rootChecked.addView(mTvCheck);
    super.onInitUI();
    rootChecked.setOnClickListener(this);
    mCheckedCount = mSelectedMedias.size();
    setCheckedCount();
    //        StateListDrawable stateListDrawable = new StateListDrawable();
    //        ImageConfig config = ResourceConfigHelper.getInstance().getImageConfig();
    //        Drawable checked;
    //        if (config != null && !TextUtils.isEmpty(config.getSelectedImage())) {
    //            checked = getResources().getDrawable(ResourceConfigHelper.getInstance().
    //                    getIdentifier(this, config.getSelectedImage(), "drawable"));
    //        } else {
    //            checked = getResources().getDrawable(R.drawable.ic_selected_image);
    //        }
    //        Drawable unChecked;
    //        if (config != null && !TextUtils.isEmpty(config.getUnselectedImage())) {
    //            unChecked = getResources().getDrawable(ResourceConfigHelper.getInstance().
    //                    getIdentifier(this, config.getUnselectedImage(), "drawable"));
    //        } else {
    //            unChecked = getResources().getDrawable(R.drawable.ic_unselected_image);
    //        }
    //        stateListDrawable.addState(new int[]{android.R.attr.state_checked}, checked);
    //        stateListDrawable.addState(new int[]{-android.R.attr.state_checked}, unChecked);
    //        mTvCheck.setBackground(stateListDrawable);
  }

  private void setCheckedCount() {
    mTvCount.setText(String.valueOf(mCheckedCount));
    mSendView.setClickable(mCheckedCount > 0);

  }

  AbstractDraweeControllerBuilder mDraweeControllerBuilder;

  public AbstractDraweeControllerBuilder getDraweeControllerBuilder() {
    if (mDraweeControllerBuilder == null) {
      Fresco.initialize(this);
      mDraweeControllerBuilder = Fresco.newDraweeControllerBuilder();
    }
    return mDraweeControllerBuilder;
  }

  @Override
  protected void setTitle() {
    super.setTitle();
    //        mTvCheck.setChecked(medias.get(position).isChecked());
    updateCheckedState();
  }

  private void updateCheckedState() {
    boolean checked = medias.get(position).isChecked();
    ImageConfig config = ResourceConfigHelper.getInstance().getImageConfig();
    if (config != null && !TextUtils.isEmpty(config.getSelectedImage()) && !TextUtils.isEmpty(config.getUnselectedImage())) {
      //            mTvCheck.setSource();
      FrameLayout rootChecked = (FrameLayout) findViewById(R.id.layout_check);
      ViewGroup.LayoutParams lp = new FrameLayout.LayoutParams(60, 60);
      mTvCheck.setLayoutParams(lp);
      WritableArray array = new WritableNativeArray();
      WritableMap map = new WritableNativeMap();
      map.putString("uri", checked ? config.getSelectedImage() : config.getUnselectedImage());
      array.pushMap(map);
      mTvCheck.setSource(array);
      mTvCheck.maybeUpdateView();
    } else {
      mTvCheck.setBackgroundResource(checked ? R.drawable.ic_selected_image : R.drawable.ic_unselected_image);
    }
  }

  @Override
  protected View onCreateItemView(LayoutInflater inflater, Media media, int position) {
    View view = inflater.inflate(media.isVideo() ? R.layout.video_player_view_item : media.isGif() ? R.layout.view_gif : R.layout.view_avater_zoom, null);
    view.setTag(position);
    return view;
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.activity_select_media_scale_image;
  }

  @Override
  public void updateItemView(Media media, View itemView) {
    if (!media.isVideo()) {
      if (media.isGif()) {
        GifImageView gifImageView = itemView.findViewById(R.id.iv_gif);
        try {
          GifDrawable gifDrawable = new GifDrawable(media.getPath());
          gifImageView.setImageDrawable(gifDrawable);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        ProgressBar progressBar = (ProgressBar) itemView
          .findViewById(R.id.pb_progress);
        PhotoView imageViewTouch = (PhotoView) itemView
          .findViewById(R.id.iv_avater);
        imageViewTouch.setProgress(progressBar);
        imageViewTouch.setImagePath(media.getPath(), UIUtils.getWidthPixels(), UIUtils.getHeightPixels());
      }
    } else {
      VideoPlayerView videoPlayerView = (VideoPlayerView) itemView;
      videoPlayerView.setVideoPath(media.getPath());
      if (medias.size() == 1 && media.isVideo()) {
        // 如果只选择了一个视频则直接自动播放
        videoPlayerView.startPlay();
      }
    }
  }

  @Override
  public void onPageSelected(int position) {
    super.onPageSelected(position);
    checkToPauseVideo(position - 1);
    checkToPauseVideo(position + 1);
  }

  /**
   * 检查暂停播放视频
   *
   * @param position
   */
  private void checkToPauseVideo(int position) {
    if (position < 0 || medias == null || position >= medias.size()) {
      return;
    }
    Media media = medias.get(position);
    ViewPager viewPager = getViewPager();
    if (media.isVideo()) {
      VideoPlayerView videoPlayerView = (VideoPlayerView) viewPager.findViewWithTag(position);
      if (videoPlayerView != null) {
        videoPlayerView.resetMediaPlayer();
      }
    }
  }

  @Override
  public List<Media> getMedias(Intent intent) {
    return intent.getParcelableArrayListExtra(EXTRA_MEDIAS);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.layout_check) {
      Media media = medias.get(position);
      onImageCheckedClicked(media);
    } else if (id == R.id.title_left_img) {
      onBackResult(false);
    } else if (id == R.id.layout_send) {
      onBackResult(true);
    }
  }

  @Override
  public void onBackPressed() {
    onBackResult(false);
    return;
  }

  private void updateCheckedList(Media media) {
    if (media.isChecked()) {
      mSelectedMedias.add(media);
    } else {
      if (mSelectedMedias.contains(media)) {
        mSelectedMedias.remove(media);
      }
    }
    notifyDataSetChanged();
  }

  private void onBackResult(boolean success) {
    Intent intent = new Intent();
    intent.putExtra(EXTRA_SELECTED_MEDIAS, mSelectedMedias);
    intent.putExtra(EXTRA_RESULT, success);
    setResult(RESULT_OK, intent);
    finish();
  }

  private void onImageCheckedClicked(Media media) {
    if (!media.isChecked()) {
      if (mCheckedCount >= mMaxCount) {
        ToastUtils.showToast(mMaxCount > 1 ? getString(R.string.send_image_max_count, mMaxCount) : getString(R.string.send_image_only_one));
        return;
      }
    }
    //        mTvCheck.toggle();
    media.setChecked(!media.isChecked());
    updateCheckedState();
    updateCheckedList(media);
    mCheckedCount = media.isChecked() ? mCheckedCount + 1 : mCheckedCount - 1;
    setCheckedCount();
  }
}
