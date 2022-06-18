package com.atom.plugin.logger.compile


import com.android.annotations.VisibleForTesting;
import com.android.ide.common.internal.WaitableExecutor;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


abstract class BuildExecutor : Executor {

    companion object{
        private var sHasWaitableExecutor = false

        init {
            sHasWaitableExecutor = try {
                // 2.3.3版本下execute(Ljava/util/concurrent/Callable;)不是ForkJoinTask
                val waitableExecutorClazz =
                    Class.forName("com.android.ide.common.internal.WaitableExecutor")
                val method: Method =
                    waitableExecutorClazz.getMethod("execute", Callable::class.java)
                method.returnType === ForkJoinTask::class.java
            } catch (e: Throwable) {
                false
            }
        }

        open fun createExecutor(): BuildExecutor? {
            return if (sHasWaitableExecutor) BuildWaitableExecutor() else CacheWaitableExecutor()
        }
    }

    internal class BuildWaitableExecutor : BuildExecutor() {
        private val mExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        @kotlin.jvm.Throws(InterruptedException::class)
        override fun waitAllTaskComplete() {
            mExecutor.waitForTasksWithQuickFail<Any>(true)
        }

        override fun execute(@NotNull runnable: Runnable) {
            mExecutor.execute<Any?> {
                runnable.run()
                null
            }
        }
    }

    @VisibleForTesting
    internal class CacheWaitableExecutor : BuildExecutor() {
        private val mPoolExecutor: ThreadPoolExecutor
        private val mWaitingTaskCount: AtomicInteger = AtomicInteger(0)
        private val mDefaultThreadFactory: ThreadFactory = Executors.defaultThreadFactory()
        private var mThrowable: Throwable? = null
        private val mLock = Object()

        @Throws(InterruptedException::class)
        override fun waitAllTaskComplete() {
            checkAndThrow()
            synchronized(mLock) {
                checkAndThrow()
                mPoolExecutor.shutdown()
                if (mWaitingTaskCount.get() != 0) {
                    mLock.wait()
                    checkAndThrow()
                }
            }
            mPoolExecutor.awaitTermination(0, TimeUnit.MILLISECONDS)
        }

        private fun checkAndThrow() {
            if (mThrowable is RuntimeException) {
                throw (mThrowable as RuntimeException?)!!
            } else if (mThrowable != null) {
                throw RuntimeException(mThrowable)
            }
        }

        override fun execute(runnable: Runnable) {
            mWaitingTaskCount.incrementAndGet()
            mPoolExecutor.execute(runnable)
        }

        init {
            val processorsNum = Runtime.getRuntime().availableProcessors()
            mPoolExecutor = object : ThreadPoolExecutor(
                processorsNum,
                processorsNum * 2,
                60L,
                TimeUnit.SECONDS,
                LinkedBlockingQueue()
            ) {
                protected override fun afterExecute(runnable: Runnable?, th: Throwable?) {
                    if (mThrowable != null) {
                        return
                    }
                    if (th != null || mWaitingTaskCount.decrementAndGet() == 0) {
                        synchronized(mLock) {
                            if (mThrowable != null) {
                                return
                            }
                            if (th != null) {
                                queue.clear()
                                mThrowable = th
                            }
                            mLock.notifyAll()
                        }
                    }
                }
            }
            mPoolExecutor.setThreadFactory { runnable ->
                val result: Thread = mDefaultThreadFactory.newThread(runnable)
                result.uncaughtExceptionHandler =
                    Thread.UncaughtExceptionHandler { thread: Thread?, th: Throwable? -> }
                result
            }
        }
    }

    @kotlin.jvm.Throws(InterruptedException::class)
    abstract fun waitAllTaskComplete()



}