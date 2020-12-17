/**
 * 
 */
package com.yunio.videocapture.cache;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.yunio.videocapture.ThreadPoolManager;
import com.yunio.videocapture.utils.LogUtils;

import android.os.SystemClock;

/**
 * Cache implementation that caches files directly onto the hard disk in the
 * specified directory. The default disk usage size is 5MB, but is configurable.
 * 
 */
public class DiskFileCache {
    private static final String TAG = "DiskBasedCache";
    /** Map of the Key, CacheHeader pairs */
    private final Map<String, Long> mEntries = new LinkedHashMap<String, Long>(16, .75f, true);

    /** Total amount of space currently used by the cache in bytes. */
    private long mTotalSize = 0;

    /** The root directory to use for the cache. */
    private final File mRootDirectory;

    /** The maximum size of the cache in bytes. */
    private final int mMaxCacheSizeInBytes;

    /** Default maximum disk usage in bytes. */
    private static final int DEFAULT_DISK_USAGE_BYTES = 5 * 1024 * 1024;

    /** High water mark percentage for the cache */
    private static final float HYSTERESIS_FACTOR = 0.9f;

    /**
     * Constructs an instance of the DiskBasedCache at the specified directory.
     * 
     * @param rootDirectory
     *            The root directory of the cache.
     * @param maxCacheSizeInBytes
     *            The maximum size of the cache in bytes.
     */
    public DiskFileCache(File rootDirectory, int maxCacheSizeInBytes) {
        mRootDirectory = rootDirectory;
        mMaxCacheSizeInBytes = maxCacheSizeInBytes;
        ThreadPoolManager.getBitmapRequestThreadPool().add(new Runnable() {

            @Override
            public void run() {
                initialize();
            }
        });
    }

    /**
     * Constructs an instance of the DiskBasedCache at the specified directory
     * using the default maximum cache size of 5MB.
     * 
     * @param rootDirectory
     *            The root directory of the cache.
     */
    public DiskFileCache(File rootDirectory) {
        this(rootDirectory, DEFAULT_DISK_USAGE_BYTES);
    }

    /**
     * Clears the cache. Deletes all cached files from disk.
     */
    public synchronized void clear() {
        File[] files = mRootDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        mEntries.clear();
        mTotalSize = 0;
        LogUtils.d(TAG, "Cache cleared.");
    }

    public long getTotalSize() {
        return mTotalSize;
    }

    /**
     * Returns the cache entry with the specified key if it exists, null
     * otherwise.
     */
    public synchronized File get(String key) {
        String fileName = getFilenameForKey(key);
        if (!mEntries.containsKey(fileName)) {
            return null;
        }
        File file = getFileForName(fileName);
        file.setLastModified(System.currentTimeMillis());
        return file;
    }

    /**
     * Initializes the DiskBasedCache by scanning for all files currently in the
     * specified root directory.
     */
    public synchronized void initialize() {
        // java.lang.IllegalArgumentException: Comparison method violates its
        // general contract!
        // jdk 7的sort函数的实现变了，造成了这个问题
        // http://blog.csdn.net/sells2012/article/details/18947849
        File[] files = mRootDirectory.listFiles();
        if (files == null) {
            return;
        }
        // 将文件按最近最少使用排序(最早的文件--最新的文件)
        Arrays.sort(files, new Comparator<File>() {

            @Override
            public int compare(File lhs, File rhs) {
                return lhs.lastModified() == rhs.lastModified() ? 0
                        : (lhs.lastModified() > rhs.lastModified() ? 1 : -1);
                // return (int) (lhs.lastModified() - rhs.lastModified());
            }
        });
        for (File file : files) {
            mEntries.put(file.getName(), file.length());
        }
    }

    /**
     * Puts the entry with the specified key into the cache.
     */
    public synchronized void put(String key) {
        String fileName = getFilenameForKey(key);
        File file = getFileForName(fileName);
        if (!file.exists()) {
            LogUtils.e(TAG, "file: %s is not exists. key: %s", file.getAbsolutePath(), key);
            return;
        }
        long size = file.length();
        if (size <= 0) {
            file.delete();
            LogUtils.d(TAG, "length of file: %s is zero. key: %s", file.getAbsolutePath(), key);
            return;
        }
        pruneIfNeeded(size);
        putEntry(fileName, size);
    }

    /**
     * Removes the specified key from the cache if it exists.
     */
    public synchronized void remove(String key) {
        String fileName = getFilenameForKey(key);
        boolean removed = removeEntry(fileName);
        if (!removed) {
            LogUtils.d(TAG, "Could not delete cache entry for key=%s, filename=%s", key, fileName);
        } else {
            getFileForName(fileName).delete();
        }
    }

    /**
     * Creates a pseudo-unique filename for the specified cache key.
     * 
     * @param key
     *            The key to generate a file name for.
     * @return A pseudo-unique filename.
     */
    public static String getFilenameForKey(String key) {
        int firstHalfLength = key.length() / 2;
        String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
        localFilename += String.valueOf(key.substring(firstHalfLength).hashCode());
        return localFilename;
    }

    /**
     * Returns a file object for the given cache key.
     */
    public File getFileForKey(String key) {
        return getFileForName(getFilenameForKey(key));
    }

    public File getFileForName(String fileName) {
        return new File(mRootDirectory, fileName);
    }

    /**
     * Prunes the cache to fit the amount of bytes specified.
     * 
     * @param neededSpace
     *            The amount of bytes we are trying to fit into the cache.
     */
    private void pruneIfNeeded(long neededSpace) {
        if ((mTotalSize + neededSpace) < mMaxCacheSizeInBytes) {
            return;
        }
        LogUtils.d(TAG, "Pruning old cache entries.");

        long before = mTotalSize;
        int prunedFiles = 0;
        long startTime = SystemClock.elapsedRealtime();

        Iterator<Map.Entry<String, Long>> iterator = mEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            Long size = entry.getValue();
            final String fileName = entry.getKey();
            boolean deleted = getFileForName(fileName).delete();
            if (deleted) {
                mTotalSize -= size;
            } else {
                LogUtils.d(TAG, "Could not delete cache entry for filename=%s", entry.getKey());
            }
            iterator.remove();
            prunedFiles++;

            if ((mTotalSize + neededSpace) < mMaxCacheSizeInBytes * HYSTERESIS_FACTOR) {
                break;
            }
        }

        LogUtils.d(TAG, "pruned %d files, %d bytes, %d ms", prunedFiles, (mTotalSize - before),
                SystemClock.elapsedRealtime() - startTime);
    }

    /**
     * Puts the entry with the specified key into the cache.
     * 
     * @param fileName
     *            The key to identify the entry by.
     * @param entry
     *            The entry to cache.
     */
    private void putEntry(String fileName, long size) {
        if (!mEntries.containsKey(fileName)) {
            mTotalSize += size;
        } else {
            long oldSize = mEntries.get(fileName);
            mTotalSize += (size - oldSize);
        }
        mEntries.put(fileName, size);
    }

    /**
     * Removes the entry identified by 'key' from the cache.
     */
    private boolean removeEntry(String fileName) {
        Long size = mEntries.get(fileName);
        if (size == null) {
            return false;
        }
        mTotalSize -= size;
        mEntries.remove(fileName);
        return true;
    }
}
