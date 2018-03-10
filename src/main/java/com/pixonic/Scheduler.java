package com.pixonic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public class Scheduler {
    public enum State {
        LATENT,
        STARTED,
        CLOSED
    }

    private static final int NCPU = Runtime.getRuntime().availableProcessors();
    private static final int MAX_TASKS_PER_ITERATION = 64;
    private static final int ITERATION_PER_STEP = 3;

    private final DelayQueue<ScheduledTask> queue = new DelayQueue<>();
    private final AtomicReference<State> state = new AtomicReference<>(State.LATENT);

    private final Runnable dispatcher = this::processTasks;
    private final ExecutorService executor;

    public Scheduler(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public Scheduler() {
        this(NCPU);
    }

    public final boolean isStarted() {
        return state.get() == State.STARTED;
    }

    public final void start() {
        if (!state.compareAndSet(State.LATENT, State.STARTED))
            throw new IllegalStateException("Scheduler already started or stoped!");

        executor.execute(dispatcher);
    }

    public final void stop() {
        if (!state.compareAndSet(State.STARTED, State.CLOSED))
            throw new IllegalStateException("Scheduler already stoped or has not been started");

        executor.shutdown();
    }

    public final void schedule(LocalDateTime scheduledTime, Callable callable) {
        if (!isStarted())
            throw new IllegalStateException("Scheduler not started!");

        requireNonNull(scheduledTime, "scheduledTime can't be null");
        requireNonNull(callable, "callable can't be null");

        queue.offer(new ScheduledTask(scheduledTime, callable));
    }

    private void processTasks() {
        Optional<ScheduledTask> task = awaitTask();
        task.ifPresent(t -> executor.submit(t.getCallable()));

        submitExpiredTasks();
        executor.execute(dispatcher);
    }

    private void submitExpiredTasks() {
        List<ScheduledTask> buffer = new ArrayList<>(MAX_TASKS_PER_ITERATION);

        int transferred;
        int iter = ITERATION_PER_STEP;
        do {
            transferred = queue.drainTo(buffer, MAX_TASKS_PER_ITERATION);
            if (!buffer.isEmpty()) {
                buffer.forEach(t -> executor.submit(t.getCallable()));
                buffer.clear();
            }
        } while (transferred == MAX_TASKS_PER_ITERATION && --iter > 0);
    }

    private Optional<ScheduledTask> awaitTask() {
        try {
            return Optional.of(queue.take());
        } catch (InterruptedException ignored) {}

        return Optional.empty();
    }
}

