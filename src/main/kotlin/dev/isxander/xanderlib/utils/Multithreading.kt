package dev.isxander.xanderlib.utils

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

object Multithreading {

    private val counter = AtomicInteger(0)
    private val SCHEDULED_POOL = ScheduledThreadPoolExecutor(
        20
    ) { r: Runnable? ->
        Thread(
            r,
            "Scheduled Thread ${counter.incrementAndGet()}"
        )
    }
    private val POOL = ThreadPoolExecutor(
        20, 20, 0L, TimeUnit.SECONDS, LinkedBlockingQueue()
    ) { r: Runnable? -> Thread(r, String.format("Thread #%s", counter.incrementAndGet())) }

    fun scheduleAsync(runnable: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*>? {
        return SCHEDULED_POOL.schedule(runnable, delay, unit)
    }

    fun runAsync(runnable: Runnable) {
        POOL.execute(runnable)
    }

}