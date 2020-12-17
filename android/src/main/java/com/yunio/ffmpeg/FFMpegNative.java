package com.yunio.ffmpeg;

/**
 * Protocol: FFmpeg类库支持的协议 AVFormat: FFmpeg类库支持的封装格式 AVCodec: FFmpeg类库支持的编解码器
 * AVFilter: FFmpeg类库支持的滤镜
 * 
 */
public class FFMpegNative {

    public static native int ffmpegcore(int argc, String[] argv);

    public static native String avcodecInfo();

//    static {
//        System.loadLibrary("avutil-55");
//        System.loadLibrary("swresample-2");
//        System.loadLibrary("avcodec-57");
//        System.loadLibrary("avformat-57");
//        System.loadLibrary("swscale-4");
//        System.loadLibrary("avfilter-6");
////        System.loadLibrary("avdevice-57");
//        System.loadLibrary("ffmpeg_codec");
//    }

     static {
     System.loadLibrary("avutil-54");
     System.loadLibrary("swresample-1");
     System.loadLibrary("avcodec-56");
     System.loadLibrary("avformat-56");
     System.loadLibrary("swscale-3");
     System.loadLibrary("postproc-53");
     System.loadLibrary("avfilter-5");
     System.loadLibrary("avdevice-56");
     System.loadLibrary("ffmpeg_codec");
     }

}
