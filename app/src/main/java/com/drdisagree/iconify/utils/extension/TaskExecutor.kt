package com.drdisagree.iconify.utils.extension

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.Volatile

abstract class TaskExecutor<Params, Progress, Result> {

    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()
    private val mCancelled: AtomicBoolean
    private val preExecuteLatch: CountDownLatch
    private val isCancelled: Boolean
        get() = mCancelled.get()

    @Volatile
    var status: Status
        private set

    init {
        status = Status.PENDING
        mCancelled = AtomicBoolean(false)
        preExecuteLatch = CountDownLatch(1)
    }

    @SafeVarargs
    fun execute(vararg params: Params) {
        if (status != Status.PENDING) {
            when (status) {
                Status.RUNNING -> throw IllegalStateException("Cannot execute task: the task is already running.")
                Status.FINISHED -> throw IllegalStateException("Cannot execute task: the task has already been executed (a task can be executed only once)")
                Status.PENDING -> {}
            }
        }

        mExecutor.execute {
            status = Status.RUNNING

            try {
                mHandler.post {
                    if (!isCancelled) {
                        onPreExecute()
                    }

                    preExecuteLatch.countDown()
                }

                preExecuteLatch.await()

                if (!isCancelled) {
                    val result = runInBackground(*params)

                    mHandler.post {
                        if (!isCancelled) {
                            onPostExecute(result)
                        }
                    }
                }
            } catch (throwable: Throwable) {
                mCancelled.set(true)

                try {
                    throw throwable
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            } finally {
                if (isCancelled) {
                    onCancelled()
                }

                status = Status.FINISHED
            }
        }
    }

    protected abstract fun onPreExecute()

    @SafeVarargs
    private fun runInBackground(vararg params: Params): Result? {
        return if (!isCancelled) {
            doInBackground(*params)
        } else null
    }

    protected abstract fun doInBackground(vararg params: Params): Result

    @SafeVarargs
    protected fun publishProgress(vararg values: Progress) {
        if (!isCancelled) {
            mHandler.post { onProgressUpdate(*values) }
        }
    }

    protected open fun onProgressUpdate(vararg values: Progress) {}
    protected abstract fun onPostExecute(result: Result?)
    protected open fun onCancelled() {}

    fun cancel(mayInterruptIfRunning: Boolean) {
        mCancelled.set(true)
        if (mayInterruptIfRunning) {
            mHandler.removeCallbacksAndMessages(null)
        }
    }

    enum class Status {
        PENDING,
        RUNNING,
        FINISHED
    }
}
