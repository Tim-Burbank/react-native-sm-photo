package com.yunio.videocapture.activity;

import android.Manifest;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.yunio.easypermission.BasePermissionActivity;
import com.yunio.easypermission.PermissionCallBackM;
import com.yunio.photoplugin.R;
import com.yunio.videocapture.ThreadPoolManager;
import com.yunio.videocapture.entity.Folder;
import com.yunio.videocapture.entity.Folder.Media;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.ResourceUtils;
import com.yunio.videocapture.resource.entity.StringConfig;
import com.yunio.videocapture.utils.FileUtils;
import com.yunio.videocapture.utils.ProgressDialogUtils;
import com.yunio.videocapture.utils.StatusBarUtils;
import com.yunio.videocapture.utils.UIUtils;
import com.yunio.videocapture.utils.Utils;
import com.yunio.videocapture.utils.ViewUtils;
import com.yunio.videocapture.utils.WindowUtils;
import com.yunio.videocapture.view.AnimationLayout;
import com.yunio.videocapture.view.ImageViewNet;

import java.util.List;

public abstract class SelectImageBaseActivity extends BasePermissionActivity
  implements OnClickListener, PermissionCallBackM {
  private static final int REQUEST_CODE_READ_PERMISSION = 1001;
  private final static int TYPE_CAMERA = 0;
  private final static int TYPE_IMAGE = 1;
  private GridView gvFolders;
  private AnimationLayout animLayout;
  private ListView lvContent;
  private TextView tvSelect;

  protected int mImageSize;
  private List<Folder> mImageFolders;
  protected Folder mFolder;
  private LayoutInflater mLayoutInflater;
  private ImageAdapter mImageAdapter;
  private FolderAdapter mFolderAdapter;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutResId());
    StatusBarUtils.setTranslucent(this, true);
    StatusBarUtils.setColor(this, Color.parseColor("#000000"), true);
    StatusBarUtils.setStyle(this, "light-content");
    onInitUI();
    requestPermission(REQUEST_CODE_READ_PERMISSION,
      new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
      getString(R.string.need_permission_tips), this);
  }

  /**
   * 是否是选择头像操作
   *
   * @return
   */
  public boolean isSelectAvatar() {
    return false;
  }

  public boolean isSupportGif() {
    return true;
  }

  protected void onInitUI() {
    mImageSize = ViewUtils.getAdapterItemSize(4, UIUtils.dip2px(1), UIUtils.getWidthPixels());
    gvFolders = (GridView) findViewById(R.id.gv_local_image_folder);
    lvContent = (ListView) findViewById(R.id.lv_folder);
    animLayout = (AnimationLayout) findViewById(R.id.anim_layout);
    tvSelect = (TextView) findViewById(R.id.tv_select);
    tvSelect.setOnClickListener(this);
    findViewById(R.id.title_left_img).setOnClickListener(this);
    ResourceUtils.initTitleBack(getWindow().getDecorView());
    View contentView = this.findViewById(android.R.id.content);
    contentView.setPadding(0, WindowUtils.getStatusBarHeight(this), 0, 0);
    mLayoutInflater = LayoutInflater.from(this);
    StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
    String allPhoto = getString(R.string.all_photo);
    if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getAllPhoto())) {
      allPhoto = stringConfig.getAllPhoto();
    }
    String videoPhoto = getString(R.string.all_video_photo);
    if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getAllVideoPhoto())) {
      videoPhoto = stringConfig.getAllVideoPhoto();
    }
    tvSelect.setText(isSelectAvatar() ? allPhoto : videoPhoto);
    TextView titleMiddle = findViewById(R.id.title_middle_text);
    if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getPreview())) {
      titleMiddle.setText(stringConfig.getCamera());
    }
  }

  private class ImageAdapter extends BaseAdapter {

    public ImageAdapter() {

    }

    @Override
    public int getCount() {
      int count = 0;
      if (showCamera()) {
        count = 1;
      }
      if (mFolder == null) {
        return count;
      }
      return mFolder.getCount() + count;
    }

    @Override
    public Folder.Media getItem(int position) {
      return mFolder.get(position);
    }

    @Override
    public int getViewTypeCount() {
      return showCamera() ? 2 : 1;
    }

    @Override
    public int getItemViewType(int position) {
      if (showCamera() && position == 0) {
        return TYPE_CAMERA;
      }
      return TYPE_IMAGE;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      int type = getItemViewType(position);
      boolean isImage = type == TYPE_IMAGE;
      if (convertView == null) {
        convertView = mLayoutInflater.inflate(getItemResId(), null);
      }
      ViewHolder holder = (ViewHolder) convertView.getTag();
      if (holder == null) {
        holder = getViewHolder(convertView);
        convertView.setTag(holder);
      }
      if (isImage) {
        Media media;
        if (showCamera()) {
          media = getItem(position - 1);
        } else {
          media = getItem(position);
        }
        holder.iveAvatar.setScaleType(ScaleType.CENTER_CROP);
        if (media.isVideo()) {
          holder.iveAvatar.setVideoPath(media.getPath(), mImageSize, mImageSize);
        } else {
          holder.iveAvatar.setImagePath(media.getPath(), mImageSize, mImageSize);
        }
        updateImageItem(convertView, isImage, position, media);
      } else {
        holder.iveAvatar.setScaleType(ScaleType.CENTER);
        holder.iveAvatar.setImageResource(R.drawable.camera);
        updateImageItem(convertView, false, position, null);
      }
      return convertView;
    }
  }

  protected void updateImageItem(View convertView, boolean isImage, int position, Media media) {

  }

  public int getItemResId() {
    return R.layout.adapter_image;
  }

  public ViewHolder getViewHolder(View convertView) {
    return new ViewHolder(convertView);
  }

  protected class ViewHolder {
    ImageViewNet iveAvatar;
    ImageView mCtvAvatar;

    public ViewHolder(View convertView) {
      initView(convertView);
    }

    public void initView(View convertView) {
      iveAvatar = (ImageViewNet) convertView.findViewById(R.id.ive_avatar);
      mCtvAvatar = (ImageView) convertView.findViewById(R.id.iv_checked);
      LayoutParams lp = new LayoutParams(mImageSize, mImageSize);
      iveAvatar.setLayoutParams(lp);
      mCtvAvatar.setLayoutParams(lp);
    }
  }

  class FolderAdapter extends BaseAdapter {
    private int mImageSize;

    public FolderAdapter() {
      mImageSize = ViewUtils.getSmallSize();
    }

    @Override
    public int getCount() {
      return Utils.isEmpty(mImageFolders) ? 0 : mImageFolders.size();
    }

    @Override
    public Folder getItem(int position) {
      return mImageFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = mLayoutInflater.inflate(R.layout.adapter_folder, null);
      }
      FolderHolder holder = (FolderHolder) convertView.getTag();
      if (holder == null) {
        holder = new FolderHolder();
        LayoutParams lp = new LayoutParams(mImageSize, mImageSize);
        holder.iveAvatar = (ImageViewNet) convertView.findViewById(R.id.ive_avatar);
        holder.iveAvatar.setLayoutParams(lp);
        holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
        holder.tvNum = (TextView) convertView.findViewById(R.id.tv_num);
        holder.ivSelect = (ImageView) convertView.findViewById(R.id.iv_select);
        Drawable select = holder.ivSelect.getDrawable();
        if (select != null) {
          select.setColorFilter(ResourceUtils.getThemeColor(), PorterDuff.Mode.SRC_IN);
        }
        convertView.setTag(holder);
      }
      final Folder folder = getItem(position);
      String nums = getString(R.string.total_x_photo, folder.getCount());
      StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
      if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getTotalXPhoto())) {
        nums = String.format(stringConfig.getTotalXPhoto(), folder.getCount());
      }
      holder.tvName.setText(folder.getName());
      holder.tvNum.setText(nums);
      holder.ivSelect.setVisibility(mFolder == folder ? View.VISIBLE : View.GONE);
      if (!Utils.isEmpty(folder.getChildList()) && folder.getChildList().get(0).isVideo()) {//所有视频
        Media media = folder.getChildList().get(0);
        holder.iveAvatar.setVideoPath(media.getPath(), mImageSize, mImageSize);
      } else {
        holder.iveAvatar.setImagePath(folder.getChild(), mImageSize, mImageSize);
      }

      convertView.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          loadFolder(folder);
        }
      });
      return convertView;
    }
  }

  protected void loadFolder(Folder folder) {
    mFolder = folder;
    tvSelect.setText(folder.getName());
    handFolderAdapter();
    handleImageAdapter();
    animLayout.dismiss();
  }

  protected void handleImageAdapter() {
    if (mImageAdapter == null) {
      mImageAdapter = new ImageAdapter();
      gvFolders.setAdapter(mImageAdapter);
    } else {
      mImageAdapter.notifyDataSetChanged();
    }
  }

  protected void handFolderAdapter() {
    if (mFolderAdapter == null) {
      mFolderAdapter = new FolderAdapter();
      lvContent.setAdapter(mFolderAdapter);
    } else {
      mFolderAdapter.notifyDataSetChanged();
    }
  }

  class FolderHolder {
    ImageViewNet iveAvatar;
    TextView tvName;
    TextView tvNum;
    ImageView ivSelect;
  }

  protected boolean showCamera() {
    return false;
  }

  @Override
  public void onBackPressed() {
    if (animLayout.isShowing()) {
      animLayout.dismiss();
      return;
    }
    super.onBackPressed();
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.tv_select) {
      animLayout.show();
    } else if (id == R.id.title_left_img) {
      finish();
    }
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
        StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
        String allPhoto = getString(R.string.all_photo);
        if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getAllPhoto())) {
          allPhoto = stringConfig.getAllPhoto();
        }
        //                final List<Folder> folders = isSelectAvatar() ? FileUtils.getImageFolders(SelectImageBaseActivity.this, allPhoto) : FileUtils.getVideoImages(SelectImageBaseActivity.this,
        //                        getString(R.string.all_video_photo));
        final List<Folder> folders = isSelectAvatar() ? FileUtils.getImageFolders(SelectImageBaseActivity.this, allPhoto, isSupportGif()) : FileUtils.getVideoImages(SelectImageBaseActivity.this,
          getString(R.string.all_video_photo), isSupportGif());
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            ProgressDialogUtils.dismissProgressDialog();
            mImageFolders = folders;
            if (!Utils.isEmpty(folders)) {
              mFolder = folders.get(0);
            }
            handFolderAdapter();
            handleImageAdapter();
          }
        });

      }
    });
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

  protected abstract int getLayoutResId();
}
