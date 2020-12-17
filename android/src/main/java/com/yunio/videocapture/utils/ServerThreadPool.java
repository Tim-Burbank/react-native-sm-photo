package com.yunio.videocapture.utils;

import android.os.Process;
import android.util.Log;

import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerThreadPool {

    public static final String TAG = "ServerThreadPool";

    public static final int TASK_LEVEL_LOW = 0;
    public static final int TASK_LEVEL_NORMAL = 1;
    public static final int TASK_LEVEL_HIGH = 2;

    private PriorityBlockingQueue<Runnable> mWorkQueue;

    private ThreadPoolExecutor mExecutor;
    /*
     * 任务加入到队列时的次序
     */
    private static final AtomicInteger mOrder = new AtomicInteger(1);

    /*
     * 私化构造方法
     */
    private ServerThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        mWorkQueue = new PriorityBlockingQueue<Runnable>();
        mExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 10, TimeUnit.MICROSECONDS,
                mWorkQueue);
    }


    public static ServerThreadPool newInstance(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        return new ServerThreadPool(corePoolSize, maximumPoolSize, keepAliveTime);
    }

    public static ServerThreadPool newInstance(int corePoolSize,
                                               int maximumPoolSize) {
        return newInstance(corePoolSize, maximumPoolSize, 10);
    }

    public static ServerThreadPool newInstance() {
        return newInstance(2, 4);
    }

    public static ServerThreadPool newSingleThreadInstance() {
        return newInstance(1, 1, 0);
    }


    /**
     * 向线程池中投递一个runnable
     *
     * @param task     任务
     * @param priority 任务的优先级
     * @return alway true
     */
    public boolean add(Runnable task, int priority) {
        if (null == task) {
            throw new RuntimeException("params is error when call fetch()");
        }

        // 自动生成优先级与次序
        priority = mOrder.getAndIncrement();
        mExecutor.execute(new TaskWithPriority(task, priority, priority));
        Log.v(TAG, TAG + ", queue:" + mWorkQueue);
        return true;
    }

    /**
     * 向线程池中投递一个runnable
     *
     * @param task 任务
     * @return always true
     */
    public boolean add(Runnable task) {
        return add(task, 0);
    }

    /**
     * 移除之前向线程池中投递的runnable
     *
     * @param task 任务
     * @return always true
     */
    public boolean remove(Runnable task) {
        return true;
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are
     * executed, but no new tasks will be accepted. Invocation has no additional
     * effect if already shut down. This method does not wait for previously
     * submitted tasks to complete execution. Use awaitTermination to do that.
     */
    public void shutDown() {
        mExecutor.shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of
     * waiting tasks, and returns a list of the tasks that were awaiting
     * execution. These tasks are drained (removed) from the task queue upon
     * return from this method. This method does not wait for actively executing
     * tasks to terminate. Use awaitTermination to do that. There are no
     * guarantees beyond best-effort attempts to stop processing actively
     * executing tasks. This implementation cancels tasks via Thread.interrupt,
     * so any task that fails to respond to interrupts may never terminate.
     *
     * @return list of tasks that never commenced execution.
     */
    public List<Runnable> shutDownNow() {
        return mExecutor.shutdownNow();
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown request,
     * or the timeout occurs, or the current thread is interrupted, whichever
     * happens first.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return true if this executor terminated and false if the timeout elapsed
     * before termination
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return mExecutor.awaitTermination(timeout, unit);
    }

    /*
     * 有优先级别的任务
     */
    class TaskWithPriority implements Comparable<TaskWithPriority>, Runnable {

        private int mPriority;
        private Runnable mRunnable;
        private int mOrder;

        public TaskWithPriority(Runnable runnable, int level) {
            mRunnable = runnable;
            mPriority = level;
        }

        public TaskWithPriority(Runnable runnable, int level, int order) {
            mRunnable = runnable;
            mPriority = level;
            mOrder = order;
        }

        @Override
        public void run() {
            // 设置后台线程的优先级
            Process.setThreadPriority(Process.myTid(),
                    Process.THREAD_PRIORITY_BACKGROUND);

            // long startTime = System.currentTimeMillis();
            mRunnable.run();
        }

        @Override
        public int compareTo(TaskWithPriority o) {
            // 降序排列
            return -(mPriority - o.mPriority);
        }

        @Override
        public String toString() {
            return "priority:" + mPriority + ";order:" + mOrder;
        }
    }
}
