package com.pixonic;

import java.util.Optional;
import java.util.concurrent.Executors;

public class SingleThreadScheduler extends AbstractScheduler {
    public SingleThreadScheduler() {
        super(Executors.newSingleThreadExecutor());
    }

    @Override
    protected void processTasks() {
        Optional<ScheduledTask> task = awaitTask();
        task.ifPresent(t -> getExecutor().submit(t.getCallable()));

        submitExpiredTasks();
        getExecutor().execute(getDispatcher());
    }
}
