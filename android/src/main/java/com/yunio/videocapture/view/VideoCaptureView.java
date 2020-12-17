package com.yunio.videocapture.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunio.photoplugin.R;
import com.yunio.videocapture.resource.ResourceUtils;
import com.yunio.videocapture.utils.ViewUtils;

import java.util.Formatter;
import java.util.Locale;

public class VideoCaptureView extends VideoCaptureBaseView {
    private final static String TAG = "VideoCaptureView";
    private final static int PERIOD_TIME = 1000;
    private ImageView mIvCameraBtn;
    private TextView mTvRecordTime;
    private long mStartRecordTime;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private static final int WHAT_UPDATE_RECORD_TIME = 1;
    boolean isTakePicture = false;//是否是拍照模式
    boolean isdoingSavePicture = false;//相机是否正在拍照保存
    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == WHAT_UPDATE_RECORD_TIME) {
                int recordTime = (int) (System.currentTimeMillis() - mStartRecordTime);
                String strTime = stringForTime(recordTime);
                mTvRecordTime.setText(strTime);

                sendEmptyMessageDelayed(WHAT_UPDATE_RECORD_TIME, PERIOD_TIME);
            }
        }
    };
    private TakePatureInterface mTakePatureInterface;
    private View mLayoutButton;
    private ImageView mIvReverseCameraBtn;

    public VideoCaptureView(Context context) {
        super(context);
    }

    public VideoCaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onInitView(View rootView) {
        super.onInitView(rootView);
        mIvCameraBtn = (ImageView) rootView.findViewById(R.id.tv_camera);
        mTvRecordTime = (TextView) rootView.findViewById(R.id.tv_record_time);
        mTvRecordTime.setTextColor(ResourceUtils.getThemeColor());
        mLayoutButton = rootView.findViewById(R.id.layout_button);
        setCameraColorFilter();
        mIvReverseCameraBtn = (ImageView) rootView.findViewById(R.id.iv_reverse_camera);
        mIvCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTakePicture && mTakePatureInterface != null) {
                    if (!isdoingSavePicture) {
                        isdoingSavePicture = true;
                        mTakePatureInterface.didTakePicture();
                    }
                    return;
                }
                boolean isRecording = isRecording();
                if (isRecording) {
                    mRecordingInterface.didRecordStop(false, false);
                    mIvReverseCameraBtn.setVisibility(View.VISIBLE);
                } else {
                    mRecordingInterface.didRecordStart();
                    mIvReverseCameraBtn.setVisibility(View.GONE);
                }
                mIvCameraBtn.setImageResource(isRecording ? R.drawable.ic_capture_btn : R.drawable.ic_capturing_btn);
                setCameraColorFilter();
            }
        });
    }

    private void setCameraColorFilter(){
        Drawable camera = mIvCameraBtn.getDrawable();
        if(camera!=null){
            camera.setColorFilter(ResourceUtils.getThemeColor(),PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.capture_video_layout;
    }

    public void updateCameraBottomUI(boolean isTakePicture) {
        if (isTakePicture) {
//            mIvCameraBtn.setImageResource(R.drawable.bg_capture_normal);
            mIvCameraBtn.setImageDrawable(ResourceUtils.getCameraNormalDrawable());
            mLayoutButton.setBackgroundColor(getResources().getColor(R.color.black_alph_60));
        } else {
            mLayoutButton.setBackgroundResource(R.drawable.camera_video_bg);
        }

    }

    @Override
    public void onRecordingStopped(String message) {
        super.onRecordingStopped(message);
        mMainHandler.removeMessages(WHAT_UPDATE_RECORD_TIME);
        ViewUtils.setVisibility(mTvRecordTime, View.GONE);
    }

    @Override
    public void onRecordingStarted() {
        super.onRecordingStarted();
        ViewUtils.setVisibility(mTvRecordTime, View.VISIBLE);
        mStartRecordTime = System.currentTimeMillis();
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        mMainHandler.sendEmptyMessageDelayed(WHAT_UPDATE_RECORD_TIME, 0);
    }

    public void setTakePictureInterface(TakePatureInterface takePictureInterface) {
        mTakePatureInterface = takePictureInterface;
    }

    public void setTakeCameraMode(boolean takePicture) {
        isTakePicture = takePicture;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public boolean isdoingSavePicture() {
        return isdoingSavePicture;
    }

    public void setIsdoingSavePicture(boolean isdoingSavePicture) {
        this.isdoingSavePicture = isdoingSavePicture;
    }

    public void setRecordBtmClickable(boolean clickable){
        mIvCameraBtn.setClickable(clickable);
    }

    public void updateRecordButtonBg(){
        mIvReverseCameraBtn.setVisibility(isRecording() ? View.VISIBLE : View.GONE);
        mIvCameraBtn.setImageResource(isRecording() ? R.drawable.ic_capture_btn : R.drawable.ic_capturing_btn);
    }
}
