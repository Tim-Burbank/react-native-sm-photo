package com.yunio.videocapture.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.yunio.photoplugin.R;
import com.yunio.videocapture.utils.LogUtils;
import com.yunio.videocapture.utils.UIUtils;
import com.yunio.videocapture.utils.VideoUtils;

/**
 * 视频播放View
 * Created by PeterZhang on 2018/5/24.
 */

public class VideoPlayerView extends FrameLayout {
    private static final String TAG = "VideoPlayerView";
    // 有效的点击偏移量
    private static final int VALID_CLICK_OFFSET = UIUtils.dip2px(4);
    private static final int VALID_CLICK_INTERVAL = 500;
    // 保存按下去的点的位置信息
    private float mDownPointX;
    private float mDownPointY;
    private long mActionDownTime;

    private String mPath;
    private AppVideoView videoView;
    private ImageView mIvPlayer;
    private ImageView mIvnVideoCover;

    private int mVideoSeekPosition;
    private boolean mRestoreSeekBar;

    private MediaController mMediaController;

    private boolean configured;

    public VideoPlayerView(Context context) {
        super(context);
        init(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        View rootView = View.inflate(context, R.layout.video_player_view_layout, this);
        videoView = (AppVideoView) rootView.findViewById(R.id.vv_video);
        mIvPlayer = (ImageView) rootView.findViewById(R.id.iv_player);
        mIvnVideoCover = (ImageView) rootView.findViewById(R.id.iv_video_cover);
        videoView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Log.d(TAG, "mRestoreSeekBar: " + mRestoreSeekBar + ", mVideoSeekPosition: " + mVideoSeekPosition);
                if (!mRestoreSeekBar) {
                    return;
                }
                if (mVideoSeekPosition > 0) {
                    // 切回前台，恢复播放
                    final int seekPosistion = mVideoSeekPosition;
                    mVideoSeekPosition = 0;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!videoView.isPlaying()) {
                                videoView.seekTo(seekPosistion);
                                videoView.start();
                            }
                        }
                    }, 10);
                    mRestoreSeekBar = false;
                } else {
                    // 在恢复VideoView时并不一定要播放，但此时VideoView会调用openView之后显示出MediaController
                    // 重置VideoView
                    resetMediaPlayer();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                // 如果嵌套在ViewPager中可能当前并没有播放但也会调用surfaceDestroyed
                mRestoreSeekBar = true;
                mIvnVideoCover.setVisibility(View.VISIBLE);
            }
        });

        mMediaController = new VideoControllerView(context);
        videoView.setMediaController(mMediaController);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility != View.VISIBLE && videoView.isPlaying()) {
            mVideoSeekPosition = videoView.getCurrentPosition();
        }
        Log.d(TAG, "onWindowVisibilityChanged visible: " + visibility + ", mVideoSeekPosition: " + mVideoSeekPosition);
        super.onWindowVisibilityChanged(visibility);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean isDisallowIntercept = true;
        int action = motionEvent.getAction();
        boolean isInterest = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownPointX = motionEvent.getX();
                mDownPointY = motionEvent.getY();
                isInterest = true;
                mActionDownTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!checkValidClick(motionEvent.getX(), motionEvent.getY())) {
                    isDisallowIntercept = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (checkValidClick(motionEvent.getX(), motionEvent.getY())) {
                    onVideoClick();
                }
                break;
            default:
                isDisallowIntercept = false;
                break;
        }
        getParent().requestDisallowInterceptTouchEvent(isDisallowIntercept);
        return isInterest;
    }

    private boolean checkValidClick(float curX, float curY) {
        return isValidClick(curX, curY)
                && ((System.currentTimeMillis() - mActionDownTime) < VALID_CLICK_INTERVAL);
    }

    public void setVideoPath(String path) {
        LogUtils.d(TAG, "setVideoPath: " + path);
        this.mPath = path;
        videoView.setVideoPath(mPath);
        mIvnVideoCover.setImageDrawable(new ColorDrawable(Color.BLACK));
        //        updateVideoCover(path);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //播放结束后的动作
                resetMediaPlayer();
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        // 隐藏视频封面
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START || what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            LogUtils.d(TAG, "listener width : " + videoView.getMeasuredWidth() + " height : " + videoView.getMeasuredHeight());
                            if (!configured) {
                                configured = true;
                                int videoWidth = mp.getVideoWidth();
                                int videoHeight = mp.getVideoHeight();
                                LogUtils.d(TAG, "video width : " + videoWidth + " height : " + videoHeight);
                                if (videoWidth > 0 && videoHeight > 0) {
                                    float scale = (float) videoWidth / (float) videoHeight;
                                    LogUtils.d(TAG, "scale : " + scale);
                                    videoWidth = videoView.getMeasuredWidth();
                                    videoHeight = (int) (videoWidth / scale);
                                    if (videoHeight > videoView.getMeasuredHeight()) {
                                        videoHeight = videoView.getMeasuredHeight();
                                    }
                                    LogUtils.d(TAG, "result width : " + videoWidth + " height : " + videoHeight);
                                    //  int deslocationX = (int) (videoWidth / 2.0 - videoView.getMeasuredWidth() / 2.0);
                                    //  videoView.animate().translationX(-deslocationX);
                                    videoView.setMeasured(videoWidth, videoHeight);
                                    videoView.requestLayout();
                                }
                            }
                        }
                        mIvnVideoCover.setVisibility(View.GONE);
                        return true;
                    }
                });
            }
        });
    }

    private void updateVideoCover(String videoPath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (bitmap != null) {
            bitmap = VideoUtils.extractBigThumbnail(bitmap);
            mIvnVideoCover.setImageBitmap(bitmap);
        }
        mIvnVideoCover.setImageDrawable(new ColorDrawable(Color.BLACK));
    }

    public void startPlay() {
        if (!videoView.isPlaying()) {
            playVideo();
        }
    }

    /**
     * 重置MediaPlayer
     */
    public void resetMediaPlayer() {
        LogUtils.d(TAG, "resetMediaPlayer: " + mPath);
        videoView.pause();
        videoView.suspend();
        resetState();
    }

    protected void onVideoClick() {
        if (videoView.getDuration() < 0) {
            playVideo();
        }
    }

    private void onPauseBtnClick() {
        //        if (videoView.isPlaying()) {
        //            pauseVideo();
        //        } else {
        //            playVideo();
        //        }
    }

    private void pauseVideo() {
        videoView.pause();
        updatePlayerBtnState(false);
    }

    private void playVideo() {
        if (videoView.getDuration() < 0) {
            // videoView调用过suspend，需要resume
            videoView.resume();
        }
        videoView.start();
        updatePlayerBtnState(true);
    }

    private void resetState() {
        //        mIvnVideoCover.setVisibility(View.VISIBLE);
        updatePlayerBtnState(false);
    }

    private void updatePlayerBtnState(boolean isPlaying) {
        mIvPlayer.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
    }

    private boolean isValidClick(float upX, float upY) {
        if (upX >= mDownPointX - VALID_CLICK_OFFSET && upX <= mDownPointX + VALID_CLICK_OFFSET
                && upY >= mDownPointY - VALID_CLICK_OFFSET
                && upY <= mDownPointY + VALID_CLICK_OFFSET) {
            return true;
        }
        return false;
    }
}
