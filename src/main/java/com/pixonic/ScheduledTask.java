package com.pixonic;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

final class ScheduledTask implements Delayed {
    private static final AtomicLong sequencer = new AtomicLong(0);

    private final long seqNumber;
    private final LocalDateTime scheduledTime;
    private final Callable callable;

    ScheduledTask(LocalDateTime scheduledTime, Callable callable) {
        this.seqNumber = sequencer.getAndIncrement();
        this.scheduledTime = scheduledTime;
        this.callable = callable;
    }

    public Callable<?> getCallable() {
        return callable;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        Duration duration = Duration.between(LocalDateTime.now(), scheduledTime);
        return unit.convert(duration.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o == null) return 1;
        if (o == this) return 0;

        if (o.getClass() == ScheduledTask.class) {
            ScheduledTask t = (ScheduledTask) o;
            int diff = scheduledTime.compareTo(t.scheduledTime);
            if (diff == 0)
                if (seqNumber > t.seqNumber) {
                    return 1;
                } else {
                    return -1;
                }

            return diff;
        }

        return Long.compare(
                getDelay(TimeUnit.NANOSECONDS),
                o.getDelay(TimeUnit.NANOSECONDS)
        );
    }
}
