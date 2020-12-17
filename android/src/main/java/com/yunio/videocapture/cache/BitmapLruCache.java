/**
 * 
 */
package com.yunio.videocapture.cache;

import android.graphics.Bitmap;

/**
 * The class to implement the bitmap LRU cache mechanism.
 * 
 */
public class BitmapLruCache extends LruCache<String, Bitmap> {

    public BitmapLruCache(int maxSizeInBytes) {
        super(maxSizeInBytes);
    }

    @Override
    public int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }
}
