package com.pixonic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public abstract class AbstractScheduler implements Scheduler {
    public enum State {
        LATENT,
        STARTED,
        CLOSED
    }

    protected static final int NCPU = Runtime.getRuntime().availableProcessors();
    protected static final int MAX_TASKS_PER_ITERATION = 64;
    protected static final int ITERATION_PER_STEP = 3;

    private final DelayQueue<ScheduledTask> queue = new DelayQueue<>();
    private final AtomicReference<State> state = new AtomicReference<>(State.LATENT);

    private final Runnable dispatcher = this::processTasks;
    private final ExecutorService executor;

    protected AbstractScheduler(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public final boolean isStarted() {
        return state.get() == State.STARTED;
    }

    @Override
    public final void start() {
        if (!state.compareAndSet(State.LATENT, State.STARTED))
            throw new IllegalStateException("Scheduler already started or stoped!");

        executor.execute(dispatcher);
    }

    @Override
    public final void stop() {
        if (!state.compareAndSet(State.STARTED, State.CLOSED))
            throw new IllegalStateException("Scheduler already stoped or has not been started");

        executor.shutdown();
    }

    @Override
    public final void schedule(LocalDateTime scheduledTime, Callable callable) {
        if (!isStarted())
            throw new IllegalStateException("Scheduler not started!");

        requireNonNull(scheduledTime, "scheduledTime can't be null");
        requireNonNull(callable, "callable can't be null");

        queue.offer(new ScheduledTask(scheduledTime, callable));
    }

    protected abstract void processTasks();

    protected void submitExpiredTasks() {
        List<ScheduledTask> buffer = new ArrayList<>(MAX_TASKS_PER_ITERATION);

        int transferred;
        int iter = ITERATION_PER_STEP;
        do {
            transferred = getQueue().drainTo(buffer, MAX_TASKS_PER_ITERATION);
            if (!buffer.isEmpty()) {
                buffer.forEach(t -> getExecutor().submit(t.getCallable()));
                buffer.clear();
            }
        } while (transferred == MAX_TASKS_PER_ITERATION && --iter > 0);
    }

    protected Optional<ScheduledTask> awaitTask() {
        try {
            return Optional.of(getQueue().take());
        } catch (InterruptedException ignored) {
        }

        return Optional.empty();
    }

    protected Runnable getDispatcher() {
        return dispatcher;
    }

    protected DelayQueue<ScheduledTask> getQueue() {
        return queue;
    }

    protected ExecutorService getExecutor() {
        return executor;
    }

    protected State getState() {
        return state.get();
    }
}
