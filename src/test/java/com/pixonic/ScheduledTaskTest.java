package com.pixonic;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ScheduledTaskTest {
    @Test
    public void compareTo() {
        LocalDateTime now = LocalDateTime.now();
        ScheduledTask task1 = new ScheduledTask(now.plusSeconds(10), () -> null);
        ScheduledTask task2 = new ScheduledTask(now.plusSeconds(10), () -> null);

        long delay1 = task1.getDelay(TimeUnit.MILLISECONDS);
        long delay2 = task2.getDelay(TimeUnit.MILLISECONDS);
        assertEquals(delay1, delay2, 20);

        int c = task2.compareTo(task1);
        assertEquals(1, c);
    }

    @Test
    public void compare() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        TreeSet<ScheduledTask> tasks = new TreeSet<>();
        tasks.add(new ScheduledTask(now.plusSeconds(1), () -> "1"));
        tasks.add(new ScheduledTask(now.plusSeconds(1), () -> "2"));
        tasks.add(new ScheduledTask(now.plusSeconds(5), () -> "3"));
        tasks.add(new ScheduledTask(now.plusSeconds(1), () -> "4"));

        assertEquals(tasks.pollFirst().getCallable().call(), "1");
        assertEquals(tasks.pollFirst().getCallable().call(), "2");
        assertEquals(tasks.pollFirst().getCallable().call(), "4");
        assertEquals(tasks.pollFirst().getCallable().call(), "3");
    }
}