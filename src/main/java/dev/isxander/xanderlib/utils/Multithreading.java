package dev.isxander.xanderlib.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class Multithreading {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final ScheduledThreadPoolExecutor SCHEDULED_POOL = new ScheduledThreadPoolExecutor(20, (r) -> new Thread(r, String.format("Scheduled Thread #%s", counter.incrementAndGet())));
    public static final ThreadPoolExecutor POOL = new ThreadPoolExecutor(20, 20, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), (r) -> new Thread(r, String.format("Thread #%s", counter.incrementAndGet())));

    public static ScheduledFuture<?> scheduleAsyncAtFixedRate(Runnable runnable, long initialDelay, long delayBetweenExecution, TimeUnit unit) {
        return SCHEDULED_POOL.scheduleAtFixedRate(runnable, initialDelay, delayBetweenExecution, unit);
    }

    public static ScheduledFuture<?> scheduleAsync(Runnable runnable, long delay, TimeUnit unit) {
        return SCHEDULED_POOL.schedule(runnable, delay, unit);
    }

    public static void runAsync(Runnable runnable) {
        POOL.execute(runnable);
    }

    public static Future<?> submit(Runnable runnable) {
        return POOL.submit(runnable);
    }

}
