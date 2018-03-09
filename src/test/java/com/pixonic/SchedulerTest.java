package com.pixonic;

import org.junit.After;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.time.LocalDateTime.now;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class SchedulerTest {

    protected Scheduler scheduler;

    @After
    public void tearDown() throws Exception {
        if (scheduler.isStarted())
            scheduler.stop();
    }

    @Test
    public void start() {
        scheduler.start();
    }

    @Test
    public void stop() {
        scheduler.start();
        scheduler.stop();
    }

    @Test
    public void startAfterStop() {
        scheduler.start();
        scheduler.stop();
        assertException(scheduler::start, IllegalStateException.class, "Scheduler already started or stoped!");
    }

    @Test
    public void doubleStart() {
        scheduler.start();
        assertException(scheduler::start, IllegalStateException.class, "Scheduler already started or stoped!");
    }

    @Test
    public void stopNotStarted() {
        assertException(scheduler::stop, IllegalStateException.class, "Scheduler already stoped or has not been started");
    }

    @Test
    public void schedule() {
        scheduler.start();
        scheduler.schedule(now().plusSeconds(10), () -> "test");
    }

    @Test
    public void scheduleNotStarted() {
        assertException(() -> {
            scheduler.schedule(now().plusSeconds(10), () -> "test");
        }, IllegalStateException.class, "Scheduler not started!");
    }

    @Test
    public void scheduleAfterStoped() {
        scheduler.start();
        scheduler.stop();
        assertException(() -> {
            scheduler.schedule(now().plusSeconds(10), () -> "test");
        }, IllegalStateException.class, "Scheduler not started!");
    }

    @Test
    public void shouldNotScheduledWithNullTime() {
        scheduler.start();
        assertException(() -> {
            scheduler.schedule(null, () -> "test");
        }, NullPointerException.class, "scheduledTime can't be null");
    }

    @Test
    public void shouldNotScheduledWithNullCallable() {
        scheduler.start();
        assertException(() -> {
            scheduler.schedule(now().plusSeconds(10), null);
        }, NullPointerException.class, "callable can't be null");
    }

    @Test
    public void executeTask() throws Exception {
        scheduler.start();

        CountDownLatch latch = new CountDownLatch(1);
        scheduler.schedule(now().plusSeconds(1), () -> {
            latch.countDown();
            return null;
        });

        latch.await(2, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void executeTasksWithDifferentTime() throws Exception {
        scheduler.start();

        CountDownLatch latch = new CountDownLatch(2);
        List<String> results = new CopyOnWriteArrayList<>();
        scheduler.schedule(now().plusSeconds(2), () -> {
            try {
                return results.add("2");
            } finally {
                latch.countDown();
            }
        });
        scheduler.schedule(now().plusSeconds(1), () -> {
            try {
                return results.add("1");
            } finally {
                latch.countDown();
            }
        });

        latch.await(3, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
        assertEquals(Arrays.asList("1", "2"), results);
    }

    @Test
    public void executeTasksWithEqualsTime() throws Exception {
        scheduler.start();

        LocalDateTime time = now().plusSeconds(1);
        CountDownLatch latch = new CountDownLatch(2);
        List<String> results = new CopyOnWriteArrayList<>();
        scheduler.schedule(time, () -> {
            try {
                return results.add("2");
            } finally {
                latch.countDown();
            }
        });
        scheduler.schedule(time, () -> {
            try {
                return results.add("1");
            } finally {
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
        assertEquals(Arrays.asList("2", "1"), results);
    }

    private static void assertException(Runnable r, Class<? extends Throwable> exceptionClass, String exceptionMessage) {
        Throwable t = null;
        try {
            r.run();
        } catch (Throwable e) {
            t = e;
        }

        assertNotNull(t);
        assertEquals(exceptionClass, t.getClass());
        assertEquals(exceptionMessage, t.getMessage());
    }
}