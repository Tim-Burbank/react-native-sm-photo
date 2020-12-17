package com.yunio.videocapture.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yunio.photoplugin.R;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.ResourceUtils;
import com.yunio.videocapture.resource.entity.StringConfig;
import com.yunio.videocapture.utils.LogUtils;
import com.yunio.videocapture.utils.ToastUtils;
import com.yunio.videocapture.utils.UIUtils;
import com.yunio.videocapture.utils.ViewUtils;
import com.yunio.videocapture.utils.WindowUtils;

import java.util.Timer;
import java.util.TimerTask;

public class VideoCaptureCodeView extends VideoCaptureBaseView {
  private final static String TAG = "VideoCaptureView";
  private final static int DELAY_TIME = 100;
  private final static int DELEAY_VIDEO_TIME = 1000;
  private ProgressView mProgressView;
  private TextView mTvCamera;
  private TextView mTvState;

  private Timer mTimer;
  private int mCount;

  private boolean isAtRemove;
  private long mLastTochUpTime;
  private long mStartTouchTime;

  private GradientDrawable mNormalCamera, mPressedCamera;

  public VideoCaptureCodeView(Context context) {
    super(context);
  }

  public VideoCaptureCodeView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onInitView(View rootView) {
    super.onInitView(rootView);
    mProgressView = (ProgressView) rootView.findViewById(R.id.pv_progress);
    mTvCamera = (TextView) rootView.findViewById(R.id.tv_camera);
    mTvState = (TextView) rootView.findViewById(R.id.tv_state);
    WindowUtils.isNavigationBarExist((Activity) mContext, new WindowUtils.OnNavigationStateListener() {
      @Override
      public void onNavigationState(boolean isShowing, int height) {
        FrameLayout layoutButton = findViewById(R.id.layout_button);
        layoutButton.setPadding(0, 0, 0, height);
      }
    });

    mNormalCamera = ResourceUtils.getCameraNormalDrawable();
    mPressedCamera = ResourceUtils.getCameraPressedDrawable();
    mTvCamera.setBackground(mNormalCamera);
    String pressStart = mContext.getString(R.string.press_start);
    String moveUpCancel = mContext.getString(R.string.move_up_cancel);
    String undoCancel = mContext.getString(R.string.undo_cancel);
    StringConfig config = ResourceConfigHelper.getInstance().getStringConfig();
    if (config != null) {
      if (!TextUtils.isEmpty(config.getPressStart())) {
        pressStart = config.getPressStart();
      }
      if (!TextUtils.isEmpty(config.getMoveUpCancel())) {
        moveUpCancel = config.getMoveUpCancel();
      }
      if (!TextUtils.isEmpty(config.getUndoCancel())) {
        undoCancel = config.getUndoCancel();
      }
    }
    mTvState.setText(pressStart);
    final String finalMoveUpCancel = moveUpCancel;
    final String finalUndoCancel = undoCancel;
    final String finalPressStart = pressStart;
    mTvCamera.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
          long delayTime = System.currentTimeMillis() - mLastTochUpTime;
          mStartTouchTime = System.currentTimeMillis();
          LogUtils.e(TAG, "delaytime -- >" + delayTime + "is delaytime -- > "
            + (delayTime < DELEAY_VIDEO_TIME));
          LogUtils.d(TAG, TAG + "  onTouch action: down---------------------");
          if (delayTime < DELEAY_VIDEO_TIME) {
            return true;
          }
          boolean isRecording = mRecordingInterface.didRecordStart();
          if (isRecording) {
            mTvState.setText(finalMoveUpCancel);
            mTvCamera.setBackground(mPressedCamera);
            ViewUtils.setPaddingDrawable(mTvState, R.drawable.ic_arrow_up,
              Gravity.LEFT);
          }
          return true;
        } else if (action == MotionEvent.ACTION_MOVE) {
          LogUtils.d(TAG, TAG + "  onTouch action: move-----------------------");
          if (mIsRecording) {
            isAtRemove = event.getY() < 0;
            mTvState.setText(isAtRemove ? finalUndoCancel : finalMoveUpCancel);
            ViewUtils.setPaddingDrawable(mTvState,
              isAtRemove ? 0 : R.drawable.ic_arrow_up, Gravity.LEFT);
            mProgressView.setIsRemove(isAtRemove);
          }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
          boolean isCancel = action == MotionEvent.ACTION_CANCEL;
          mLastTochUpTime = System.currentTimeMillis();
          LogUtils.d(TAG, "mLastTochUpTime -- > " + mLastTochUpTime);
          LogUtils.d(TAG, TAG + "  onTouch action: up-----------------------");
          mTvState.setText(finalPressStart);
          ViewUtils.setPaddingDrawable(mTvState, 0, Gravity.LEFT);
          mTvCamera.setBackground(mNormalCamera);
          if (Math.abs(mLastTochUpTime - mStartTouchTime) < 2000) {
            ToastUtils.showToast(R.string.video_too_short);
            mTvState.postDelayed(new Runnable() {
              @Override
              public void run() {
                mRecordingInterface.didRecordStop(isAtRemove, true);
              }
            }, 200);
          } else {
            mRecordingInterface.didRecordStop(isAtRemove, isCancel);
          }
          isAtRemove = false;
        }
        return true;
      }
    });
  }

  @Override
  public void setMax(int max) {
    super.setMax(max);
    mProgressView.setMax(max);
  }

  @Override
  public void onRecordingStopped(String message) {
    super.onRecordingStopped(message);
    if (mTimer != null) {
      mTimer.cancel();
      mTimer.purge();
      mTimer = null;
      mCount = 0;
      mProgressView.setProgress(0);
      mProgressView.setIsRemove(false);
      mProgressView.postInvalidate();
      ViewUtils.setVisibility(mProgressView, View.GONE);
    }
  }

  protected int getContentLayoutId() {
    return R.layout.view_capture_code_video;
  }

  @Override
  public void onRecordingStarted() {
    super.onRecordingStarted();
    ViewUtils.setVisibility(mProgressView, View.VISIBLE);
    mTimer = new Timer();
    mTimer.schedule(new TimerTask() {

      @Override
      public void run() {
        mCount++;
        mProgressView.setProgress(mCount * DELAY_TIME);
        if (mCount >= mMax / 100) {
          cancel();
        }
        mProgressView.postInvalidate();
      }
    }, 0, DELAY_TIME);

  }

}
