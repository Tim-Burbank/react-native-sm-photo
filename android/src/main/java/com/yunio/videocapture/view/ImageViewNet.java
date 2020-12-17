package com.yunio.videocapture.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.yunio.photoplugin.R;
import com.yunio.videocapture.BaseInfoManager;
import com.yunio.videocapture.ThreadPoolManager;
import com.yunio.videocapture.entity.ImageSize;
import com.yunio.videocapture.utils.BitmapUtils;
import com.yunio.videocapture.utils.FileUtils;
import com.yunio.videocapture.utils.ViewUtils;

import java.io.File;
import java.lang.reflect.Field;

public class ImageViewNet extends ImageView {
    protected static final int SC_OK = 200;
    protected static final int SC_NOT_FOUND = 404;
    private final static String TAG = "ImageViewNet";
    private Context mContext;
    protected String mUrl;
    protected static final int STATUS_LOADING = 1;
    protected static final int STATUS_LOAD_SUCCESS = 2;
    protected static final int STATUS_LOAD_FAILED = 3;
    protected int mStatus;
    protected ImageSize mImageSize;
    protected ProgressBar mProgress;
    private Drawable mDefaultDrawable;
    /**
     * 是否从网络取缩略图
     */
    private boolean mIsFetchNetThumbnail;

    public ImageViewNet(Context context) {
        this(context, null);
    }

    public ImageViewNet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewNet(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageViewEx);
        try {
            if (a.hasValue(R.styleable.ImageViewEx_default_src)) {
                mDefaultDrawable = a.getDrawable(R.styleable.ImageViewEx_default_src);
                if (mDefaultDrawable != null) {
                    setImageDrawable(mDefaultDrawable);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        a.recycle();
    }

    public void setDefaultDrawable(Drawable defaultDrawable) {
        this.mDefaultDrawable = defaultDrawable;
        setImageDrawable(defaultDrawable);
    }

    /**
     * 当取得图片时调用
     *
     * @param bitmap
     */
    protected void onBitmapArrived(int resultStatus, Bitmap bitmap) {
        if (bitmap != null) {
            setImageBitmap(bitmap);
        }
    }

    public ImageSize getImageSize() {
        return mImageSize;
    }

    public void setImagePath(String path) {
        setImagePath(path, 0, 0);
    }

    public void setImagePath(String path, int width, int height) {
        if (!checkUrl(path)) {
            return;
        }
        mIsFetchNetThumbnail = false;
        initImageSize(width, height);
        loadLocalImage(false);
    }

    public void setVideoPath(String path, int width, int height) {
        if (!checkUrl(path)) {
            return;
        }
        mIsFetchNetThumbnail = false;
        initImageSize(width, height);
        loadLocalImage(true);
    }

    public void setImageDrawableDefault(Drawable drawable) {
        this.mDefaultDrawable = drawable;
        setImageDrawable(mDefaultDrawable);
    }

    public void setImageDrawableDefault(int resId) {
        this.mDefaultDrawable = mContext.getResources().getDrawable(resId);
        setImageDrawable(mDefaultDrawable);
    }

    public void setProgress(ProgressBar progress) {
        this.mProgress = progress;
    }

    /**
     * 检查是否需要做进一步操作， 并设置相关值
     *
     * @param url
     * @return
     */
    protected boolean checkUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            setImageDrawable(mDefaultDrawable);
            mUrl = "";
            return false;
        }
        if (url.equals(mUrl) && mStatus != STATUS_LOAD_FAILED) {
            return false;
        }
        mUrl = url;
        mStatus = STATUS_LOADING;
        onRequestExecute();
        setImageDrawable(mDefaultDrawable);
        return true;
    }

    protected void initImageSize(int width, int height) {
        if (mImageSize == null && width > 0 && height > 0) {
            mImageSize = new ImageSize(width, height);
        }
    }

    public void onRequestExecute() {
        if (mProgress != null) {
            ViewUtils.setVisibility(mProgress, View.VISIBLE);
        }
    }

    protected void loadLocalImage() {
        loadLocalImage(false);
    }

    protected void loadLocalImage(final boolean isVideo) {
        final String url = mUrl;
        final String id = generateId();
        File file = new File(url);
        if (!file.exists() || !file.isFile()) {
            onResponse(SC_NOT_FOUND, null, url);
            return;
        }
        Bitmap bitmap = BaseInfoManager.getInstance().getBitmapLruCache().get(id);
        if (bitmap != null) {
            onResponse(SC_OK, bitmap, url);
            return;
        }
        ThreadPoolManager.getDefaultThreadPool().add(new Runnable() {

            @Override
            public void run() {
                int width = 0;
                int height = 0;
                if (mImageSize != null) {
                    width = mImageSize.getWidth();
                    height = mImageSize.getHeight();
                }
                Bitmap thumb = null;
                if (isVideo) {
                    thumb = BitmapUtils.getVideoThumbnail(url, width, height,
                            MediaStore.Video.Thumbnails.MINI_KIND);
                    //                    thumb = FileUtils.createVideoThumbnail(url, width, height);
                } else {
                    thumb = BitmapUtils.getImageThumbnail(url, width, height);
                }

                if (thumb != null) {
                    BaseInfoManager.getInstance().getBitmapLruCache().put(id, thumb);
                }
                post(thumb, url);
            }
        });
    }

    private void post(final Bitmap thumb, final String url) {
        post(new Runnable() {
            @Override
            public void run() {
                onResponse(thumb != null ? SC_OK : -1, thumb, url);
            }
        });
    }

    protected String generateId() {
        if (mImageSize == null) {
            return mUrl;
        }
        return mUrl + "_" + mImageSize.getWidth() + "x" + mImageSize.getHeight();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mUrl = null;
    }

    protected void resetUrl() {
        mUrl = null;
    }

    public void onResponse(int statusCode, Bitmap bitmap, Object tag) {
        if (!tag.equals(mUrl)) {
            return;
        }
        if (statusCode == SC_NOT_FOUND && mIsFetchNetThumbnail) {
            // 取缩略图失败, 开始取原图
            mIsFetchNetThumbnail = false;
            return;
        }
        if (SC_OK == statusCode) {
            mStatus = STATUS_LOAD_SUCCESS;
        } else {
            mStatus = STATUS_LOAD_FAILED;
        }
        onBitmapArrived(mStatus, bitmap);
        if (mProgress != null) {
            ViewUtils.setVisibility(mProgress, View.GONE);
            mProgress = null;
        }
    }

    private int calWidth() {
        final ViewGroup.LayoutParams params = this.getLayoutParams();
        int width = 0;
        if (params != null && params.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
            width = super.getWidth();
        }
        if (width <= 0 && params != null) {
            width = params.width;
        }
        if (width <= 0) {
            width = getFieldValue(this, "mMaxWidth");
        }
        return width;
    }

    private int calHeight() {
        final ViewGroup.LayoutParams params = this.getLayoutParams();
        int height = 0;
        if (params != null && params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            height = super.getHeight();
        }
        if (height <= 0 && params != null) {
            height = params.height;
        }
        if (height <= 0) {
            height = getFieldValue(this, "mMaxHeight");
        }
        return height;
    }

    private static int getFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
        }
        return value;
    }
}
