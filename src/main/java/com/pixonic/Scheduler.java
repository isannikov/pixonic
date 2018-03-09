package com.pixonic;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public interface Scheduler {
    public boolean isStarted();
    public void start();
    public void stop();
    public void schedule(LocalDateTime scheduledTime, Callable callable);
}
