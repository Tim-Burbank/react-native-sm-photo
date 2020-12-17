package com.yunio.ffmpeg;

import android.annotation.SuppressLint;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class FfmpegUtils {

    /**
     * 压缩视频文件
     *
     * @param videoPath   原视频文件路径
     * @param outputPath  压缩后的视频文件路径
     * @param maxDuration 最长允许的视频长度(截取)，小于零时不截取
     * @param maxWidth    视频最大允许宽度, 大于0时等比例缩小
     * @param maxFps      视频最大允许帧率, 小于等于0时不起作用
     * @return
     */
    public static boolean compressVideo(String videoPath, String outputPath, int maxDuration,
                                        int maxWidth, int maxFps, int crfValue) {
        VideoInfo video = getVideoInfo(videoPath);
        if (video == null) {
            return false;
        }
        List<String> commands = new ArrayList<String>();
        commands.add("ffmpeg");
        // 部分视频会抛异常：unspecified pixel format Consider increasing the value for
        // the 'analyzeduration' and 'probesize' options
        // -analyzeduration 2147483647 -probesize 2147483647
        commands.add("-i");
        // 需要压缩的原始路径
        commands.add(videoPath);
        // 大于15秒的视频截取15秒
        if (maxDuration > 0 && video.getDuration() / 1000 > maxDuration) {
            // 截取视频
            commands.add("-ss");
            // 从0秒开始
            commands.add("0");
            // 截取时间
            commands.add("-t");
            // 单位秒
            commands.add(maxDuration + "");
        }
        // 压缩视频
        commands.add("-c:v");
        // x264编码
        commands.add("libx264");
        // 压缩速度
        commands.add("-preset");
        // 压缩速度值有：ultrafast，superfast，veryfast，faster，fast，medium，slow，slower，veryslow和placebo
        // ultrafast编码速度最快，但压缩率低，生成的文件更大，placebo则正好相反。
        // x264所取的默认值为medium。preset主要是影响编码的速度，并不会很大的影响编码出来的结果的质量。
        commands.add("veryfast");
        // 输出视频的质量
        commands.add("-crf");
        // 取值范围是0-51，默认值为23，数字越小输出视频的质量越高，同时生成的压缩文件也越大。
        // 这个选项会直接影响到输出视频的码率
        // 一般来说，压制480p用20左右，压制720p用16-18
        Log.d("compressVideo", "crfValue : " + crfValue);
        if (crfValue < 0 || crfValue > 51) {
            crfValue = 25;
        }
        commands.add(crfValue + "");
        // 调整mdat信息,将moov放到mdat之前
        commands.add("-movflags");
        commands.add("faststart");
        // 文件已存在，直接覆盖
        commands.add("-y");
        if (maxWidth > 0 && Math.min(video.getHeight(), video.getWidth()) > maxWidth) {
            commands.add("-vf");
            commands.add(getScaleParam(video.getWidth(), video.getHeight(), maxWidth));
        }
        if (maxFps > 0) {
            // 将视频的fps调整为30,部分视频帧数过高，压缩非常耗时
            commands.add("-r");
            commands.add(maxFps + "");
        }
        // 处理音频
        commands.add("-acodec");
        // acc处理格式
        commands.add("libfaac");
        // 声道1：单声道;2：立体声
        //        commands.add("-ac");
        //        commands.add("1");
        // 采样率
        commands.add("-ar");
        // 44hz
        commands.add("44100");
        //        // 比特率
                commands.add("-ab");
                commands.add("96k");
        // 输出文件
        commands.add(outputPath);
        String[] argv = commands.toArray(new String[commands.size()]);
        Integer argc = argv.length;
        FFMpegNative.ffmpegcore(argc, argv);
        File outFile = new File(outputPath);
        return outFile.exists() && outFile.length() > 0;
    }

    private static String getScaleParam(int width, int height, int maxWidth) {
        // 按照宽度等比例压缩，如果高度不能被2整除，则会报错
        // 先计算等比例压缩后的高度，要是高度能被2整除，就取等比例值，否则，取能被2整除的值
        int w = width, h = height;
        if (w > h) {
            w = height;
            h = width;
        }
        float destHeight = (float) h * maxWidth / w;
//        if (destHeight % 2 == 0) {
//            return "scale=" + ((int) destHeight) + ":-1";
//        }
        int resultHeight = ((int) destHeight / 2) * 2;
        if (width > height) {
            return "scale=" + resultHeight + ":" + maxWidth;
        }
        return "scale=" + maxWidth + ":" + resultHeight;
    }

    /**
     * 取得视频的基本信息
     *
     * @param path
     * @return
     */
    public static VideoInfo getVideoInfo(String path) {
        VideoInfo video = new VideoInfo();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String duration = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (duration == null) {
            return null;
        }
        try {
            video.setDuration(Long.parseLong(duration));
            video.setWidth(Integer.parseInt(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
            video.setHeight(Integer.parseInt(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
            video.setRotation(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)); // 视频旋转方向
            String s = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
            if (s != null && !s.isEmpty()) {
                video.setFrameRate(Float.parseFloat(s));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return video;
    }
}
