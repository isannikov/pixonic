package com.pixonic;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.time.LocalDateTime.now;
import static org.junit.Assert.assertEquals;

public class MultithreadSchedulerTest extends SchedulerTest {
    @Before
    public void setUp() throws Exception {
        scheduler = new Scheduler();
    }

    @Test
    public void executeBatchTask() throws Exception {
        scheduler.start();

        int count = 70;
        CountDownLatch latch = new CountDownLatch(count);
        LocalDateTime now = now().plusSeconds(1);

        for (int i = 0; i < count; i++) {
            scheduler.schedule(now, () -> {
                latch.countDown();
                return null;
            });
        }

        latch.await(2, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
    }
}