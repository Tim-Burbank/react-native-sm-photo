package com.yunio.videocapture.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yunio.ffmpeg.FfmpegUtils;
import com.yunio.photoplugin.PhotoPluginModule;
import com.yunio.photoplugin.R;
import com.yunio.videocapture.ThreadPoolManager;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.ResourceUtils;
import com.yunio.videocapture.resource.entity.StringConfig;
import com.yunio.videocapture.utils.Constant;
import com.yunio.videocapture.utils.LogUtils;
import com.yunio.videocapture.utils.UIUtils;
import com.yunio.videocapture.utils.WindowUtils;

import java.io.File;

@SuppressLint("NewApi")
public class CaptureCodeVideoActivity extends CaptureVideoActivity {
    public static final String EXTRA_CODE = "code";
    private final static String TAG = "CaptureCodeVideoActivity";
    //    private TextView mTvCode;
    private Dialog mDialog;

    public static Intent createLauncherIntent(Context context, String code, int quality) {
        Intent intent = createVideoIntent(false, true, quality);
        intent.setClass(context, CaptureCodeVideoActivity.class);
        intent.putExtra(EXTRA_CODE, code);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView tvBack = findViewById(R.id.tv_back);
        tvBack.setOnClickListener(this);
        Drawable back = tvBack.getDrawable();
        if (back != null) {
            back.setColorFilter(ResourceUtils.getThemeColor(), PorterDuff.Mode.SRC_IN);
        }
        TextView tvReadCode = findViewById(R.id.tv_read_code);
        StringConfig config = ResourceConfigHelper.getInstance().getStringConfig();
        if (config != null && !TextUtils.isEmpty(config.getPleaseReadCode())) {
            tvReadCode.setText(config.getPleaseReadCode());
        }
        FrameLayout layouttop = findViewById(R.id.layout_top);
        layouttop.setPadding(0,WindowUtils.getStatusBarHeight(this),0,0);
        TextView mTvCode = findViewById(R.id.tv_code);
        mTvCode.setTextColor(ResourceUtils.getThemeColor());
        String code = getIntent().getStringExtra(EXTRA_CODE);
        if (!TextUtils.isEmpty(code)) {
            mTvCode.setText(code);
        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_capture_code_video;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_back) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            finish();
        }
    }

    @Override
    protected void onRecordComplete(final String path) {
        mDialog = createWaitDialog();
        try {
            mDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ThreadPoolManager.getDefaultThreadPool().add(new Runnable() {

            @Override
            public void run() {
                compressVideo(path);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        finishAndDistributeResult(path);
                    }
                });
            }
        });
    }

    private Dialog createWaitDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        String waitCompress = getString(R.string.wait_compress);
        StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
        if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getWaitCompress())) {
            waitCompress = stringConfig.getWaitCompress();
        }
        dialog.setMessage(waitCompress);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    @SuppressLint("LongLogTag")
    private void compressVideo(final String path) {
        String outPath = path + "-tmp.mp4";
        File srcFile = new File(path);
        long srcLen = srcFile.length();
        boolean success = FfmpegUtils.compressVideo(path, outPath, 0, getMaxWidth(), 30, mVideoQuality);
        if (success) {
            File outFile = new File(outPath);
            Log.d(TAG, "compress file, srcLen: " + srcLen + ", nowLen: " + outFile.length());
            outFile.renameTo(srcFile);
        }
    }

    protected void finishAndDistributeResult(String path) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        Intent dataIntent = new Intent();
        dataIntent.putExtra(Constant.EXTRA_VIDEO_PATH, path);
        //        setResult(RESULT_OK, dataIntent);
        PhotoPluginModule.onActivityResult(PhotoPluginModule.REQUEST_CODE_RECORD_VIDEO, RESULT_OK, dataIntent);
        finish();
    }

    @Override
    protected void onPause() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onPause();
    }
}
