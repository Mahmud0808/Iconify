package com.drdisagree.iconify.ui.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TaskExecutor<Params, Progress, Result> {

    private final Handler mHandler;
    private final Executor mExecutor;
    private final AtomicBoolean mCancelled;
    private final CountDownLatch preExecuteLatch;
    private volatile Status mStatus;

    public TaskExecutor() {
        mExecutor = Executors.newSingleThreadExecutor();
        mHandler = new Handler(Looper.getMainLooper());
        mStatus = Status.PENDING;
        mCancelled = new AtomicBoolean(false);
        preExecuteLatch = new CountDownLatch(1);
    }

    public final Status getStatus() {
        return mStatus;
    }

    @SafeVarargs
    public final void execute(final Params... params) {
        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task: the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task: the task has already been executed (a task can be executed only once)");
            }
        }

        mExecutor.execute(() -> {
            mStatus = Status.RUNNING;

            try {
                mHandler.post(() -> {
                    if (!isCancelled()) {
                        onPreExecute();
                    }
                    preExecuteLatch.countDown();
                });

                preExecuteLatch.await();

                if (!isCancelled()) {
                    final Result result = runInBackground(params);

                    mHandler.post(() -> {
                        if (!isCancelled()) {
                            onPostExecute(result);
                        }
                    });
                }
            } catch (Throwable throwable) {
                mCancelled.set(true);

                try {
                    throw throwable;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                if (isCancelled()) {
                    onCancelled();
                }

                mStatus = Status.FINISHED;
            }
        });
    }

    protected abstract void onPreExecute();

    @SafeVarargs
    private final Result runInBackground(Params... params) {
        if (!isCancelled()) {
            return doInBackground(params);
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "unsafe"})
    protected abstract Result doInBackground(Params... params);

    @SafeVarargs
    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            mHandler.post(() -> onProgressUpdate(values));
        }
    }

    @SuppressWarnings({"unchecked", "unsafe"})
    protected void onProgressUpdate(Progress... values) {
    }

    protected abstract void onPostExecute(Result result);

    protected void onCancelled() {
    }

    public final boolean isCancelled() {
        return mCancelled.get();
    }

    public final void cancel(boolean mayInterruptIfRunning) {
        mCancelled.set(true);
        if (mayInterruptIfRunning) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public enum Status {
        PENDING,
        RUNNING,
        FINISHED,
    }
}
