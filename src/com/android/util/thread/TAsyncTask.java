package com.android.util.thread;

import java.util.ArrayDeque;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;

/**
*
* @since 1.5
* @author Doug Lea
*/
public abstract class TAsyncTask<Params, Progress, Result> {
    private static final String LOG_TAG = "TAsyncTask";

    private static final int CORE_POOL_SIZE = 4;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(10);
    
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, LOG_TAG + "#" + mCount.getAndIncrement());
        }
    };

    public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                    TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory,new ThreadPoolExecutor.DiscardOldestPolicy());
    
    public static final SerialExecutor SERIAL_EXECUTOR = new SerialExecutor();

    private static final int MESSAGE_POST_RESULT = 0x1;
    private static final int MESSAGE_POST_PROGRESS = 0x2;

    private static final InternalHandler sHandler = new InternalHandler();
  
    private static volatile SerialExecutor sDefaultExecutor = SERIAL_EXECUTOR;
    private final WorkerRunnable<Params, Result> mWorker;
    private final TagFutureTask<Result> mFuture;

    private volatile Status mStatus = Status.PENDING;
    
    private final AtomicBoolean mCancelled = new AtomicBoolean();
    private final AtomicBoolean mTaskInvoked = new AtomicBoolean();

    private static class SerialExecutor{
        final ArrayDeque<TagFutureTask<?>> mPendingTasks = new ArrayDeque<TagFutureTask<?>>();
        private  ConcurrentHashMap<String, TagFutureTask<?>> mPendingMaps = new ConcurrentHashMap<String, TagFutureTask<?>>();
        private  ConcurrentHashMap<String, TagFutureTask<?>> mRunningMaps = new ConcurrentHashMap<String, TagFutureTask<?>>();
        
        @SuppressWarnings({ "rawtypes"})
        public synchronized void execute(TagFutureTask<?> r) {
            if(r.tag != null && (mPendingMaps.containsKey(r.tag) || mRunningMaps.containsKey(r.tag))) {
                Log.d(LOG_TAG, "--->scheduleT is exist:");
                return ;
            }
            if(r.tag != null) {
                mPendingMaps.put(r.tag, r);
            }
            mPendingTasks.addFirst(new TagFutureTask(r, r.tag));
            scheduleNext(null);
        }

        protected synchronized void scheduleNext(String preTaskTag) {
            if(preTaskTag != null) {
                mRunningMaps.remove(preTaskTag);
            }
            int count = THREAD_POOL_EXECUTOR.getCorePoolSize() - THREAD_POOL_EXECUTOR.getActiveCount();
            Log.d(LOG_TAG, "--->scheduleTNext:" + mPendingTasks.size() + "/" + count);
            for (int i = 0; i < count; i++) {
                TagFutureTask<?> task = mPendingTasks.poll();
                if (task != null) {
                    if (task.tag != null) {
                        mPendingMaps.remove(task.tag);
                        mRunningMaps.put(task.tag, task);
                    }
                    THREAD_POOL_EXECUTOR.execute(task);
                }
            }
        }
                
        public synchronized void cancelSchedule(String tag) {
            if(tag != null && mPendingMaps.containsKey(tag)) {
                TagFutureTask<?> task = mPendingMaps.get(tag);
                task.cancel(true);
                mPendingTasks.remove(task);
            }
            if(tag != null && mRunningMaps.containsKey(tag)) {
                TagFutureTask<?> task = mRunningMaps.get(tag);
                task.cancel(true);
                mRunningMaps.remove(tag);
            }
        }
        
        public synchronized void cancel() {
            mPendingTasks.clear();
            mPendingMaps.clear();
            Enumeration<TagFutureTask<?>> itors = mRunningMaps.elements();
            TagFutureTask<?> one = null;
            while(itors.hasMoreElements()) {
                one = itors.nextElement();
                one.cancel(true);
            }
            mRunningMaps.clear();
        }
    }
    
    /** 取消Tag关联的任务*/
    public static void cancalTask(String tag) {
        SERIAL_EXECUTOR.cancelSchedule(tag);
    }
    
    /** 取消所有的任务*/
    public static void cancalAll() {
        SERIAL_EXECUTOR.cancel();
    }

    public enum Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,
        /**
         * Indicates that the task is running.
         */
        RUNNING,
        /**
         * Indicates that {@link AsyncTask#onPostExecute} has finished.
         */
        FINISHED,
    }

    public static void init() {
        sHandler.getLooper();
    }

    public static void setDefaultExecutor(SerialExecutor exec) {
        sDefaultExecutor = exec;
    }
    
    public TAsyncTask() {
        this(null);
    }

    public TAsyncTask(String tag) {
        mWorker = new WorkerRunnable<Params, Result>() {
            public Result call() throws Exception {
                mTaskInvoked.set(true);

                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                return postResult(doInBackground(mParams));
            }
        };

        mFuture = new TagFutureTask<Result>(mWorker, tag) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(get());
                } catch (InterruptedException e) {
                    android.util.Log.w(LOG_TAG, e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("An error occured while executing doInBackground()",
                            e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                }
            }
        };
    }

    private void postResultIfNotInvoked(Result result) {
        final boolean wasTaskInvoked = mTaskInvoked.get();
        if (!wasTaskInvoked) {
            postResult(result);
        }
    }

    private Result postResult(Result result) {
        @SuppressWarnings("unchecked")
        Message message = sHandler.obtainMessage(MESSAGE_POST_RESULT,
                new AsyncTaskResult<Result>(this, result));
        message.sendToTarget();
        return result;
    }
    
    public final Status getStatus() {
        return mStatus;
    }

    protected abstract Result doInBackground(Params... params);

    protected void onPreExecute() {
    }

    protected void onPostExecute(Result result) {
    }
    
    protected void onProgressUpdate(Progress... values) {
    }

    protected void onCancelled(Result result) {
        onCancelled();
    }    

    protected void onCancelled() {
    }

    public final boolean isCancelled() {
        return mCancelled.get();
    }

    public final boolean cancel(boolean mayInterruptIfRunning) {
        mCancelled.set(true);
        return mFuture.cancel(mayInterruptIfRunning);
    }

    public final Result get() throws InterruptedException, ExecutionException {
        return mFuture.get();
    }

    public final Result get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return mFuture.get(timeout, unit);
    }

    public final TAsyncTask<Params, Progress, Result> execute(Params... params) {
        return executeOnExecutor(sDefaultExecutor, params);
    }

    public final TAsyncTask<Params, Progress, Result> executeOnExecutor(SerialExecutor exec,
            Params... params) {
        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)");
            }
        }

        mStatus = Status.RUNNING;

        onPreExecute();

        mWorker.mParams = params;
        exec.execute(mFuture);

        return this;
    }

    public static void execute(Runnable runnable) {
        sDefaultExecutor.execute(new TagFutureTask(runnable));
    }

    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            sHandler.obtainMessage(MESSAGE_POST_PROGRESS,
                    new AsyncTaskResult<Progress>(this, values)).sendToTarget();
        }
    }

    private void finish(Result result) {
        if (isCancelled()) {
            onCancelled(result);
        } else {
            onPostExecute(result);
        }
        mStatus = Status.FINISHED;
    }

    private static class InternalHandler extends Handler {
        @SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
        @Override
        public void handleMessage(Message msg) {
            AsyncTaskResult result = (AsyncTaskResult) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    // There is only one result
                    result.mTask.finish(result.mData[0]);
                    break;
                case MESSAGE_POST_PROGRESS:
                    result.mTask.onProgressUpdate(result.mData);
                    break;
            }
        }
    }

    private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
        Params[] mParams;
    }

    @SuppressWarnings({"RawUseOfParameterizedType"})
    private static class AsyncTaskResult<Data> {
        final TAsyncTask mTask;
        final Data[] mData;

        AsyncTaskResult(TAsyncTask task, Data... data) {
            mTask = task;
            mData = data;
        }
    }
    
    private static class TagFutureTask<Result> extends FutureTask<Result>{
        public String tag;
        
        public TagFutureTask(Callable<Result> callable, String tag) {
            super(callable);
            this.tag = tag;
        }
        
        public TagFutureTask(Runnable runable) {
            super(runable, null);
        }
        
        public TagFutureTask(Runnable runable, String tag) {
            this(runable);
            this.tag = tag;
        }
        
        @Override
        public void run() {
            try {
                super.run();
            } finally {
                SERIAL_EXECUTOR.scheduleNext(tag);
            }
        }
    }
}