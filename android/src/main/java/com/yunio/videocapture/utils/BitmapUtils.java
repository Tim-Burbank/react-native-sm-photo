package com.yunio.videocapture.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.View.MeasureSpec;

import com.yunio.videocapture.BaseInfoManager;
import com.yunio.videocapture.cache.BitmapLruCache;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {
    public static Bitmap formateViewtoBitmap(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    public static Bitmap convertViewToBitmap(View view, int bitmapWidth, int bitmapHeight) {
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));

        return bitmap;
    }

    public static boolean saveImage(Bitmap bitmap, String absPath) {
        return saveBitmap(bitmap, absPath, 100);
    }

    public static boolean saveBitmap(Bitmap bitmap, String absPath, int quality) {
        try {
            FileOutputStream fos = new FileOutputStream(absPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 根据byte数组和目标宽高计算并返回Options
     *
     * @param data
     * @param desireWidth
     * @param desireHeight
     * @return
     */
    public static Options calcSizeOptions(byte[] data, int desireWidth, int desireHeight) {
        Options options = createDecodeBoundsOptions();
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        calcSizeOptions(options, desireWidth, desireHeight);
        return options;
    }

    public static Options calcSizeOptions(String imagePath, int desireWidth, int desireHeight) {
        Options options = createDecodeBoundsOptions();
        BitmapFactory.decodeFile(imagePath, options);
        calcSizeOptions(options, desireWidth, desireHeight);
        return options;
    }

    /**
     * 根据options计算图片所需字节数量
     *
     * @param options
     * @return
     */
    public static int calcSizeByOptions(Options options) {
        return (options.outWidth * options.outHeight * BYTE_PER_PIXEL) / options.inSampleSize;
    }

    /**
     * 生成一个方形的bitmap
     *
     * @param source
     * @return
     */
    public static Bitmap squareBitmap(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int size = width < height ? width : height;
        if (width == height) {
            return source;
        }
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        return Bitmap.createBitmap(source, x, y, size, size);
    }

    /**
     * 根据最小边(取宽高中较小的值为基准)压缩图片
     *
     * @param srcPath
     * @param destPath
     * @param destWidth
     * @param maxFileSize
     * @param quarity     当>0且maxFileSize比宽高压缩后的文件大小小时只做一次质量的压缩
     * @return
     */
    public final static boolean compressByMinSize(String srcPath, String destPath, int destWidth,
                                                  int maxFileSize, int quarity) {
        return compress(srcPath, destPath, destWidth, Integer.MAX_VALUE, maxFileSize, true,
                quarity);
    }

    /**
     * <pre>
     * &#64;time Apr 3, 2014
     *
     * &#64;param srcPath
     *            path of local file
     * &#64;param destPath
     *            dest path when is compressed
     * &#64;param maxFileSize
     *            max size of dest file to compress
     * &#64;param maxMinSize
     *            max size of min from(destWidth, destHeight)
     * &#64;param maxLargeSize
     * 			  max size of max from(destWidth, destHeight)
     * &#64;param override
     *            if destPath is already exists file, if override, it will
     *            override it.if not do nothing
     * &#64;throws it's invalid and do nothing when both maxWidth and maxHeight is -1
     * </pre>
     */
    public final static boolean compress(String srcPath, String destPath, int maxMinSize,
                                         int maxLargeSize, long maxFileSize, boolean override, int targetQuality) {
        if (maxMinSize <= 0 || maxFileSize <= 0 || TextUtils.isEmpty(srcPath)) {
            return false;
        }
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            return false;
        }
        if (FileUtils.exists(destPath)) {
            if (!override) {
                return false;
            }
            FileUtils.delete(destPath);
        }
        final long srcFileSize = srcFile.length();

        // read image with and height
        Options options = getOptions(srcPath);
        final int srcWidth = options.outWidth;
        final int srcHeight = options.outHeight;

        // the min size is maxMin
        float destRatio = 1.0f;
        float minSize = 0;
        float maxSize = 0;
        if (srcWidth > srcHeight) {
            minSize = srcHeight;
            maxSize = srcWidth;
        } else {
            minSize = srcWidth;
            maxSize = srcHeight;
        }
        if (minSize > maxMinSize) {
            destRatio = ((float) maxMinSize) / minSize;
        }
        if (maxSize > maxLargeSize) {
            destRatio = Math.min((float) maxLargeSize / maxSize, destRatio);
        }
        int destWidth = (int) (srcWidth * destRatio);
        int destHeight = (int) (srcHeight * destRatio);

        // read bitmap from sdcard
        Bitmap bitmap = getBitmap(srcPath, destWidth, destHeight, true);
        if (bitmap == null) {
            return false;
        }

        destWidth = bitmap.getWidth();
        destHeight = bitmap.getHeight();
        File outputFile = new File(destPath);
        FileOutputStream out = null;
        try {
            int quality = targetQuality;
            float ratio = 0;
            // calculate quality
            ratio = ((float) (srcWidth * srcHeight)) / (destWidth * destHeight);
            long losssLessSize = (long) (srcFileSize / ratio);
            if (maxFileSize >= losssLessSize) {
                // 当前文件大小已经满足要求的文件大小
                quality = 100;
            } else if (targetQuality <= 0) {
                quality = (int) Math.floor(100.00 * maxFileSize / losssLessSize);
                if (quality <= 0) {
                    quality = 1;
                } else if (quality > 100) {
                    quality = 100;
                }
            }
            long start = System.currentTimeMillis();
            long curSize = 0;
            int ceilQuality = quality > 70 ? 100 : quality + 30;
            int floorQuality = quality < 30 ? 0 : quality - 30;
            while (true) {
                if (out != null) {
                    out.close();
                }
                out = new FileOutputStream(outputFile);
                BufferedOutputStream stream = new BufferedOutputStream(out);
                boolean success = bitmap.compress(CompressFormat.JPEG, quality, stream);
                if (!success) {
                    throw new IOException("compress bitmap failed, srcPath: " + srcPath);
                }
                stream.flush();
                stream.close();
                curSize = FileUtils.size(destPath);
                if (targetQuality > 0) {
                    // 指定了图片压缩质量， 对质量只压缩一次
                    break;
                }
                if (curSize - maxFileSize <= 5 * 1024
                        || Math.abs(ceilQuality - floorQuality) <= 1) {
                    // 压缩的文件大小与要求的文件大小之间相差很小, 或者质量已不能再小
                    break;
                }
                if (curSize < maxFileSize) {
                    floorQuality = quality;
                } else {
                    ceilQuality = quality;
                }
                quality = (ceilQuality + floorQuality) / 2;
            }
            long duration = System.currentTimeMillis() - start;
            if (LogUtils.isDebug()) {
                LogUtils.d(TAG,
                        "compress-dest-file-size " + srcFileSize + "|" + maxFileSize + " ratio="
                                + ratio + " quality=" + quality + " duration=" + duration + " size:"
                                + destWidth + "x" + destHeight + "|" + srcWidth + "x" + srcHeight
                                + " finalFileSize=" + FileUtils.size(destPath));
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        FileUtils.delete(destPath);

        return false;

    }

    /**
     * 根据宽高取图片
     *
     * @param absPath
     * @param width
     * @param height
     * @param equalRatio 是否等比例
     * @return
     */
    public static Bitmap getBitmap(String absPath, int width, int height, boolean equalRatio) {
        if (!FileUtils.exists(absPath)) {
            LogUtils.e(TAG, "invalid parameters absPath " + absPath + " width " + width + " height "
                    + height);
            return null;
        }

        // if (width <= 0 || height <= 0) {
        // return BitmapFactory.decodeFile(absPath);
        // }

        LogUtils.d(TAG, "decode bitmap " + absPath + " width " + width + " height " + height);
        Bitmap bitmap = null;
        Options options = getOptions(absPath);
        calcSizeOptions(options, width, height);
        LogUtils.d(TAG, "decode bitmap inSampleSize=%d, %dx%d", options.inSampleSize,
                options.outWidth, options.outHeight);
        try {
            bitmap = BitmapFactory.decodeFile(absPath, options);
        } catch (OutOfMemoryError error) {
            if (trimMemeryCache(options)) {
                try {
                    bitmap = BitmapFactory.decodeFile(absPath, options);
                } catch (OutOfMemoryError er) {

                }
            }
        }
        if (bitmap == null) {
            return null;
        }
        LogUtils.d(TAG, "decode bitmap size=%dx%d", bitmap.getWidth(), bitmap.getHeight());

        Matrix matrix = new Matrix();
        int exifOritation = getExifOritation(absPath);

        int rotate = getRotate(exifOritation);
        if (exifOritation == ExifInterface.ORIENTATION_TRANSVERSE) {
            rotate = 270;
        }
        matrix.setRotate(rotate);
        LogUtils.d(TAG, "decode bitmap rotate=%d", rotate);
        boolean exchange = rotate % 180 == 90;
        float ratioW = 0;
        float ratioH = 0;
        if (width > 0 && height > 0) {
            ratioW = ((float) (exchange ? height : width)) / bitmap.getWidth();
            ratioH = ((float) (exchange ? width : height)) / bitmap.getHeight();
            if (equalRatio) {
                ratioW = Math.min(ratioW, ratioH);
                ratioH = ratioW;
            }
        }
        //        if (rotate > 0) {
        //            //            matrix.postScale(ratioW, ratioH);
        //            matrix.setRotate(rotate);
        //        } else
        if (exifOritation == ExifInterface.ORIENTATION_TRANSVERSE) {
            // 前置摄像头拍出的效果
            //            matrix.setRotate(270);
            ratioW = -ratioW;
            //            matrix.postScale(-1, 1);
        }
        if (ratioW != 0 && ratioH != 0) {
            matrix.postScale(ratioW, ratioH);
        }
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);

        if (rotated == null) {
            return bitmap;
        }
        LogUtils.d(TAG, "decode bitmap final=%d x %d", rotated.getWidth(), rotated.getHeight());
        return rotated;
    }

    public static Bitmap getImageThumbnail(String absPath, int width, int height) {
        Bitmap bitmap = getBitmap(absPath, width, height, true);
        if (width > 0 && height > 0 && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, bitmap.getWidth(), bitmap.getHeight(),
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);
        // final int color =0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPX = bitmap.getWidth() / 2;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        // paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return outBitmap;
    }

    private static final String TAG = "BitmapUtils";

    /**
     * @param sourceBitmap
     * @param destWidth
     * @param destHeight
     * @param uniform      是否等比缩放
     * @return
     */
    public final static Bitmap zoom(Bitmap sourceBitmap, int destWidth, int destHeight,
                                    boolean uniform) {
        if (sourceBitmap == null) {
            return null;
        }
        if (destHeight <= 0 || destWidth <= 0) {
            LogUtils.d(TAG,
                    "invalid paraments destWidth = " + destWidth + ";  destHeight=" + destHeight);
            return sourceBitmap;
        }
        float srcWidth = sourceBitmap.getWidth();
        float srcHeight = sourceBitmap.getHeight();
        float widthRatio = destWidth / srcWidth;
        float heightRatio = destHeight / srcHeight;
        if (widthRatio >= 1 || heightRatio >= 1) {
            return sourceBitmap;
        }
        Matrix matrix = new Matrix();
        matrix.reset();
        if (uniform) {
            float resultScale = widthRatio > heightRatio ? heightRatio : widthRatio;
            matrix.postScale(resultScale, resultScale);
        } else {
            matrix.postScale(widthRatio, heightRatio);
        }

        Bitmap resizedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, (int) srcWidth,
                (int) srcHeight, matrix, true);
        // recycle(sourceBitmap);
        return resizedBitmap;
    }

    /**
     * 客服插件需要使用
     *
     * @param imagePath
     * @return
     */
    public static int getRotate(String imagePath) {
        int exifOritation = getExifOritation(imagePath);
        return getRotate(exifOritation);
    }

    public static int getRotate(int exifOritation) {
        int rotate = 0;
        switch (exifOritation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            default:
                break;
        }
        LogUtils.e(TAG, "image rotate " + rotate);
        return rotate;
    }

    private static int getExifOritation(String absPath) {
        if (!FileUtils.exists(absPath)) {
            LogUtils.e(TAG, "invalid file path");
            return ExifInterface.ORIENTATION_UNDEFINED;
        }

        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(absPath);
        } catch (IOException e) {
            e.printStackTrace();
            return ExifInterface.ORIENTATION_UNDEFINED;
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        return orientation;
    }

    // 每个像素点所占用字节数
    private static final int BYTE_PER_PIXEL = 4;

    public static Options getOptions(String filePath) {
        Options options = createDecodeBoundsOptions();
        BitmapFactory.decodeFile(filePath, options);
        return options;
    }

    /**
     * 创建用于获取图片宽高的options
     *
     * @return
     */
    private static Options createDecodeBoundsOptions() {
        Options options = new Options();
        options.inPreferredConfig = Config.ALPHA_8;
        options.inJustDecodeBounds = true;
        return options;
    }

    /**
     * 根据已取得的图片实际宽高计算缩放到目标宽高的缩放倍数
     *
     * @param options
     * @param desireWidth
     * @param desireHeight
     */
    private static void calcSizeOptions(Options options, int desireWidth, int desireHeight) {
        if (desireWidth <= 0 || desireHeight <= 0) {
            options.inSampleSize = 1;
        } else {
            options.inSampleSize = findBestSampleSize(options.outWidth, options.outHeight,
                    desireWidth, desireHeight);
        }
        options.inJustDecodeBounds = false;
    }

    /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     *
     * @param actualWidth   Actual width of the bitmap
     * @param actualHeight  Actual height of the bitmap
     * @param desiredWidth  Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    static int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth,
                                  int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }

    /**
     * 压缩图片bmp，使之小于限定值。
     *
     * @param image
     * @param maxKb 最大值 ，单位kb
     */
    public static Bitmap compressImage(Bitmap image, int maxKb) {
        long bytes = image.getRowBytes() * image.getHeight();
        if ((bytes * 1f / 1024) <= maxKb) {
            // 图片本身就小本规定大小
            return image;
        }
        LogUtils.d(TAG, "compress start Size %d", bytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        byte[] byteArray = baos.toByteArray();
        while ((byteArray.length / 1024) > maxKb) { // 循环判断如果压缩后图片是否大于maxKb,大于继续压缩
            baos.reset();
            options -= 10;// 每次都减少10
            boolean result = image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            byteArray = baos.toByteArray();
            LogUtils.d(TAG, "compress -- %d,\t byteArray %d, result %s",
                    image.getRowBytes() * image.getHeight(), byteArray.length, result);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(byteArray);
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        LogUtils.d(TAG, "compress end Size %d", bitmap.getRowBytes() * bitmap.getHeight());
        return bitmap;
    }

    public static Bitmap compressBitmap(Bitmap image, int maxKb) {
        long bytes = image.getRowBytes() * image.getHeight();
        if ((bytes * 1f / 1024) <= maxKb) {
            // 图片本身就小本规定大小
            LogUtils.d(TAG, " Size %d", bytes / 1024);
            return image;
        }
        LogUtils.d(TAG, "compress start Size %d", bytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        byte[] b = baos.toByteArray();
        // 将字节换成KB
        double mid = b.length / 1024;
        if (mid > maxKb) {
            // 获取bitmap大小 是允许最大大小的多少倍
            double i = mid / maxKb;
            // 开始压缩 此处用到平方根 将宽带和高度压缩掉对应的平方根倍Math.sqrt(i)
            image = zoomImage(image, (int) (image.getWidth() / i), (int) (image.getHeight() / i));
        }
        LogUtils.d(TAG, "compress end Size %d", image.getRowBytes() * image.getHeight());
        return image;

    }

    public static Bitmap zoomImage(Bitmap image, int newWidth, int newHeight) {
        // 获取这个图片的宽和高
        float width = image.getWidth();
        float height = image.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(image, 0, 0, (int) width, (int) height, matrix, true);
        LogUtils.d(TAG,
                "zoomImage width %s, height %s,\t  newWidth %d,  newHeight  %d,\t scaleWidth %s, scaleHeight %s, ",
                width, height, newWidth, newHeight, scaleWidth, scaleHeight);
        LogUtils.d(TAG, "zoomImage end Size %d", bitmap.getRowBytes() * bitmap.getHeight());
        return bitmap;
    }

    /**
     * 将View转换成bitmap
     *
     * @param v
     * @return
     */
    public static Bitmap generateBitmapByView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    /**
     * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static boolean trimMemeryCache(Options options) {
        return trimMemeryCache(calcSizeByOptions(options));
    }

    public static boolean trimMemeryCache(int releaseSize) {
        BitmapLruCache bitmapLruCache = BaseInfoManager.getInstance().getBitmapLruCache();
        if (bitmapLruCache.size() > 0) {
            int maxSize = bitmapLruCache.size() - releaseSize;
            if (maxSize < 0) {
                return false;
            }
            bitmapLruCache.trimToSize(maxSize);
            System.gc();
            return true;
        }
        return false;
    }

    /**
     * YUV420sp
     *
     * @param inputWidth
     * @param inputHeight
     * @param scaled
     * @return
     */
    public static byte[] getYUV420sp(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    /**
     * RGB转YUV420sp
     *
     * @param yuv420sp inputWidth * inputHeight * 3 / 2
     * @param argb     inputWidth * inputHeight
     * @param width
     * @param height
     */
    private static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        // 帧图片的像素大小
        final int frameSize = width * height;
        // ---YUV数据---
        int Y, U, V;
        // Y的index从0开始
        int yIndex = 0;
        // UV的index从frameSize开始
        int uvIndex = frameSize;

        // ---颜色数据---
        int a, R, G, B;
        //
        int argbIndex = 0;
        //

        // ---循环所有像素点，RGB转YUV---
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                // a is not used obviously
                a = (argb[argbIndex] & 0xff000000) >> 24;
                R = (argb[argbIndex] & 0xff0000) >> 16;
                G = (argb[argbIndex] & 0xff00) >> 8;
                B = (argb[argbIndex] & 0xff);
                //
                argbIndex++;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                //
                Y = Math.max(0, Math.min(Y, 255));
                U = Math.max(0, Math.min(U, 255));
                V = Math.max(0, Math.min(V, 255));

                // NV21 has a plane of Y and interleaved planes of VU each
                // sampled by a factor of 2
                // meaning for every 4 Y pixels there are 1 V and 1 U. Note the
                // sampling is every other
                // pixel AND every other scanline.
                // ---Y---
                yuv420sp[yIndex++] = (byte) Y;
                // ---UV---
                if ((j % 2 == 0) && (i % 2 == 0)) {
                    //
                    yuv420sp[uvIndex++] = (byte) V;
                    //
                    yuv420sp[uvIndex++] = (byte) U;
                }
            }
        }
    }
}
