package com.pixonic;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.PriorityQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static java.time.LocalDateTime.now;
import static org.junit.Assert.assertEquals;

public class ScheduledTaskTest {
    @Test
    public void compareTo() {
        LocalDateTime now = now();
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
        LocalDateTime now = now();

        PriorityQueue<ScheduledTask> tasks = new PriorityQueue<>();
        tasks.add(new ScheduledTask(now.plusSeconds(1), () -> "1"));
        tasks.add(new ScheduledTask(now.plusSeconds(1), () -> "2"));
        tasks.add(new ScheduledTask(now.plusSeconds(5), () -> "3"));
        tasks.add(new ScheduledTask(now.plusSeconds(1), () -> "4"));

        assertEquals(tasks.poll().getCallable().call(), "1");
        assertEquals(tasks.poll().getCallable().call(), "2");
        assertEquals(tasks.poll().getCallable().call(), "4");
        assertEquals(tasks.poll().getCallable().call(), "3");
    }

    @Test
    public void compareOtherDelayed() {
        PriorityQueue<Delayed> tasks = new PriorityQueue<>();

        OtherDelayed o1 = new OtherDelayed(now().plusSeconds(1));
        ScheduledTask o2 = new ScheduledTask(now().plusSeconds(1), () -> "1");
        OtherDelayed o3 = new OtherDelayed(now().plusSeconds(5));
        OtherDelayed o4 = new OtherDelayed(now().plusSeconds(1));

        tasks.add(o1);
        tasks.add(o2);
        tasks.add(o3);
        tasks.add(o4);

        assertEquals(o1, tasks.poll());
        assertEquals(o2, tasks.poll());
        assertEquals(o4, tasks.poll());
        assertEquals(o3, tasks.poll());
    }

    private static class OtherDelayed implements Delayed {
        final LocalDateTime time;

        private OtherDelayed(LocalDateTime time) {
            this.time = time;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            Duration duration = Duration.between(now(), time);
            return unit.convert(duration.toNanos(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(
                    getDelay(TimeUnit.NANOSECONDS),
                    o.getDelay(TimeUnit.NANOSECONDS)
            );
        }
    }
}