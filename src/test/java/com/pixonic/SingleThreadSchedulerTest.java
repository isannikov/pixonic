package com.pixonic;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.LocalDateTime.now;
import static org.junit.Assert.assertEquals;

public class SingleThreadSchedulerTest extends SchedulerTest {
    @Before
    public void setUp() throws Exception {
        scheduler = new SingleThreadScheduler();
    }

    @Test
    public void executeBatchTask() throws Exception {
        scheduler.start();

        int count = 70;
        CountDownLatch latch = new CountDownLatch(count);
        LocalDateTime now = now().plusSeconds(1);
        List<Integer> results = new CopyOnWriteArrayList<>();

        for (int i = 0; i < count; i++) {
            int id = i;
            scheduler.schedule(now, () -> {
                try {
                    return results.add(id);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(2, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
        assertEquals(IntStream.range(0, count).boxed().collect(Collectors.toList()), results);
    }
}