package com.yunio.videocapture.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.yunio.easypermission.BasePermissionActivity;
import com.yunio.easypermission.PermissionCallBackM;
import com.yunio.photoplugin.PhotoPluginModule;
import com.yunio.photoplugin.R;
import com.yunio.videocapture.ThreadPoolManager;
import com.yunio.videocapture.entity.Folder;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.ResourceUtils;
import com.yunio.videocapture.resource.entity.StringConfig;
import com.yunio.videocapture.utils.Constant;
import com.yunio.videocapture.utils.FileUtils;
import com.yunio.videocapture.utils.ProgressDialogUtils;
import com.yunio.videocapture.utils.StatusBarUtils;
import com.yunio.videocapture.utils.ToastUtils;
import com.yunio.videocapture.utils.UIUtils;
import com.yunio.videocapture.utils.Utils;
import com.yunio.videocapture.utils.ViewUtils;
import com.yunio.videocapture.utils.WindowUtils;
import com.yunio.videocapture.view.ImageViewNet;

import java.util.List;

public class SelectVideoActivity extends BasePermissionActivity
  implements OnClickListener, PermissionCallBackM {
  private final static String TAG = "SelectVideoActivity";
  private static final int REQUEST_CODE_READ_PERMISSION = 1001;
  private static final int MAX_WIDTH = 540;
  private static final int MAX_HEIGHT = 960;
  private final static String EXTRA_DURATION = "duration";
  private final static String EXTRA_SHOULD_COMPRESS = "should_compress";
  private final static String EXTRA_QUALITY = "quality";
  public static final String EXTRA_VIDEO_PATH = Constant.EXTRA_VIDEO_PATH;
  public static final String EXTRA_IMAGE_PATH = Constant.EXTRA_IMAGE_PATH;
  public static final String EXTRA_ORGINAL_DURATION = "orginal_duration";

  private GridView gvFolders;
  private int mImageSize;

  private LayoutInflater mLayoutInflater;
  private ImageAdapter mImageAdapter;
  private List<Folder.Media> mVideos;

  private int maxDuration;
  private boolean shouldCompress;
  private int mQuality;
  private AlertDialog.Builder dialog;

  public static Intent createLauncherIntent(Context context, int maxDuration, int quality, boolean shouldCompress) {
    Intent intent = new Intent(context, SelectVideoActivity.class);
    intent.putExtra(EXTRA_DURATION, maxDuration);
    intent.putExtra(EXTRA_QUALITY, quality);
    intent.putExtra(EXTRA_SHOULD_COMPRESS, shouldCompress);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_select_video);
    StatusBarUtils.setTranslucent(this, true);
    StatusBarUtils.setColor(this, Color.parseColor("#000000"), true);
    StatusBarUtils.setStyle(this, "light-content");

    initUI();
    requestPermission(REQUEST_CODE_READ_PERMISSION,
      new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA},
      getString(R.string.need_permission_tips), this);
  }

  private void initUI() {
    View contentView = this.findViewById(android.R.id.content);
    contentView.setPadding(0, WindowUtils.getStatusBarHeight(this), 0, 0);
    Intent intent = getIntent();
    maxDuration = intent.getIntExtra(EXTRA_DURATION, 0);
    shouldCompress = intent.getBooleanExtra(EXTRA_SHOULD_COMPRESS, true);
    mQuality = intent.getIntExtra(EXTRA_QUALITY, 23);
    mImageSize = ViewUtils.getAdapterItemSize(3, UIUtils.dip2px(1), UIUtils.getWidthPixels());
    gvFolders = (GridView) findViewById(R.id.gv_local_image_folder);
    findViewById(R.id.title_left_img).setOnClickListener(this);
    mLayoutInflater = LayoutInflater.from(this);
    ResourceUtils.initTitleBack(getWindow().getDecorView());
  }

  private void loadData() {
    String loading = getString(R.string.loading);
    StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
    if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getLoading())) {
      loading = stringConfig.getLoading();
    }
    ProgressDialogUtils.showProgressDialog(this, loading);
    ThreadPoolManager.getDefaultThreadPool().add(new Runnable() {

      @Override
      public void run() {
        mVideos = FileUtils.getVideoFolder(SelectVideoActivity.this);
        runOnUiThread(new Runnable() {
          public void run() {
            ProgressDialogUtils.dismissProgressDialog();
            handleImageAdapter();
          }
        });
      }
    });
  }

  protected void handleImageAdapter() {
    if (mImageAdapter == null) {
      mImageAdapter = new ImageAdapter();
      gvFolders.setAdapter(mImageAdapter);
    } else {
      mImageAdapter.notifyDataSetChanged();
    }
  }

  private class ImageAdapter extends BaseAdapter {

    public ImageAdapter() {

    }

    @Override
    public int getCount() {
      return Utils.sizeOf(mVideos);
    }

    @Override
    public Folder.Media getItem(int position) {
      return mVideos.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = mLayoutInflater.inflate(R.layout.adapter_video, null);
      }
      ViewHolder holder = (ViewHolder) convertView.getTag();
      if (holder == null) {
        holder = new ViewHolder();
        holder.iveAvatar = (ImageViewNet) convertView.findViewById(R.id.ive_avatar);
        LayoutParams lp = new LayoutParams(mImageSize, mImageSize);
        holder.iveAvatar.setLayoutParams(lp);
        holder.tvDuration = (TextView) convertView.findViewById(R.id.tv_duration);
        holder.tvDuration.setLayoutParams(lp);
        convertView.setTag(holder);
      }
      Folder.Media video = getItem(position);
      holder.iveAvatar.setScaleType(ScaleType.CENTER_CROP);
      holder.iveAvatar.setVideoPath(video.getPath(), mImageSize, mImageSize);
      holder.tvDuration.setText(Utils.resetDuration(Utils.getRoundedFloat(video.getDuration() / 1000f)));
      updateImageItem(convertView, video);
      return convertView;
    }
  }


  protected class ViewHolder {
    ImageViewNet iveAvatar;
    TextView tvDuration;
  }

  protected void updateImageItem(View convertView, final Folder.Media video) {
    convertView.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        int videoDuration = (int) video.getDuration();
        if (videoDuration < maxDuration * 1000) {
          onSelected(video);
        } else {
          showVideoToLongDialog(video);
        }
      }
    });
  }

  private void showVideoToLongDialog(final Folder.Media video) {
    String content = "";
    StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
    if (maxDuration % 60 == 0) {//是否是一分钟整数倍
      long minutes = maxDuration / 60;//分
      content = getString(R.string.video_dialog_minutes, minutes);
      if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getVideoDialogMinutes())) {
        content = String.format(stringConfig.getVideoDialogMinutes(), minutes);
      }
    } else {
      content = getString(R.string.video_dialog_seconds, maxDuration);
      if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getVideoDialogSeconds())) {
        content = String.format(stringConfig.getVideoDialogSeconds(), maxDuration);
      }
    }
    if (dialog == null) {
      //            StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
      String video_dialog_title = getString(R.string.video_dialog_title);
      if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getVideoDialogTitle())) {
        video_dialog_title = stringConfig.getVideoDialogTitle();
      }
      String cancel = getString(R.string.cancel);
      if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getCancel())) {
        cancel = stringConfig.getCancel();
      }

      dialog = new AlertDialog.Builder(this);
      dialog.setTitle(video_dialog_title).setMessage(content);
      dialog.setNegativeButton(cancel, null);

    }
    String confirm = getString(R.string.confirm);
    if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getConfirm())) {
      confirm = stringConfig.getConfirm();
    }
    dialog.setPositiveButton(confirm, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        onSelected(video);
      }
    });
    dialog.show();
  }

  private void onSelected(final Folder.Media video) {
    String waitCompress = getString(R.string.wait_compress);
    final StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
    if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getWaitCompress())) {
      waitCompress = stringConfig.getWaitCompress();
    }
    ProgressDialogUtils.showProgressDialog(this, waitCompress, false, false);
    ThreadPoolManager.getDefaultThreadPool().add(new Runnable() {

      @Override
      public void run() {
        final String videoPath = shouldCompress ? FileUtils.compressVideo(video, maxDuration, mQuality) : video.getPath();
        final String thumbNailPath = FileUtils.saveThumbnail(video);
        runOnUiThread(new Runnable() {
          public void run() {
            ProgressDialogUtils.dismissProgressDialog();
            if (TextUtils.isEmpty(thumbNailPath)) {
              String badVideo = getString(R.string.bad_video);
              if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getBadVideo())) {
                badVideo = stringConfig.getBadVideo();
              }
              ToastUtils.showToast(badVideo);
              return;
            }
            onCompressComplete(video, videoPath, thumbNailPath);
          }
        });
      }
    });
  }


  private void onCompressComplete(Folder.Media video, String videoPath, String imagePath) {
    Intent dataIntent = new Intent();
    dataIntent.putExtra(EXTRA_VIDEO_PATH, videoPath);
    dataIntent.putExtra(EXTRA_IMAGE_PATH, imagePath);
    long resultDuration = video.getDuration() < maxDuration * 1000 ? video.getDuration() : maxDuration * 1000;
    dataIntent.putExtra(EXTRA_ORGINAL_DURATION, resultDuration);
    PhotoPluginModule.onActivityResult(PhotoPluginModule.REQUEST_CODE_SELECT_VIDEO, RESULT_OK, dataIntent);
    //        setResult(RESULT_OK, dataIntent);
    finish();
  }

  @Override
  public void onClick(View arg0) {
    if (arg0.getId() == R.id.title_left_img) {
      finish();
    }
  }

  @Override
  public void onPermissionGrantedM(int requestCode, String... perms) {
    if (requestCode == REQUEST_CODE_READ_PERMISSION) {
      loadData();
    }
  }

  @Override
  public void onPermissionDeniedM(int requestCode, String... perms) {

  }
}
