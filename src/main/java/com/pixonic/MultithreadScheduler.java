package com.pixonic;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultithreadScheduler extends AbstractScheduler {
    public MultithreadScheduler(int threads) {
        super(executor(threads));
    }

    public MultithreadScheduler() {
        this(NCPU);
    }

    @Override
    protected void processTasks() {
        submitExpiredTasks();

        Optional<ScheduledTask> task = awaitTask();
        task.ifPresent(t -> getExecutor().submit(t.getCallable()));

        getExecutor().execute(getDispatcher());
    }

    private static ExecutorService executor(int threads) {
        if (threads < 2)
            throw new IllegalArgumentException("MultithreadScheduler can't be used with threads less 2");

        return Executors.newFixedThreadPool(threads);
    }
}
