package ru.covariance.processorScheduler.queue.confident;

import ru.covariance.processorScheduler.Processor;
import ru.covariance.processorScheduler.ProcessorException;
import ru.covariance.processorScheduler.queue.TaskQueue;
import ru.covariance.processorScheduler.structure.FedProcessor;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConfidentTaskQueue<T> implements TaskQueue<T> {
    private final Lock lock = new ReentrantLock(true);
    private final Condition condition = lock.newCondition();
    private final Queue<ProcessorTask<T>> queue = new ArrayDeque<>();
    private final Map<Thread, ProcessorTask<T>> inProgress = new HashMap<>();
    private final int[] epochCompletion;
    private ProcessorException exception = null;

    private final ProcessorGraph<T> graph;
    private int lastEpoch;

    public static <T> ConfidentTaskQueue<T> create(Set<Processor<T>> processors,
                                                   int maxIterations,
                                                   int maxThreads) throws ProcessorException {
        return new ConfidentTaskQueue<>(processors, maxIterations, maxThreads);
    }

    public ConfidentTaskQueue(Set<Processor<T>> processors, int maxIterations, int maxThreads) throws ProcessorException {
        this.graph = new ProcessorGraph<T>(processors, maxIterations);
        this.lastEpoch = maxIterations;
        this.epochCompletion = new int[lastEpoch];
        queue.addAll(graph.initialTasks());
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public FedProcessor<T> getTask(Thread ID) throws IllegalStateException {
        if (!isTaskAvailable()) {
            throw new IllegalStateException("There are no tasks, though getTask() is invoked");
        }
        inProgress.put(ID, queue.remove());
        return inProgress.get(ID).feed(graph);
    }

    @Override
    public void submitTask(Thread ID, T result) {
        ProcessorTask<T> current = inProgress.get(ID);
        if (current.getEpoch() > lastEpoch) {
            if (!areTaskLeft()) {
                condition.signalAll();
            }
            return;
        }
        if (result == null) {
            lastEpoch = current.getEpoch();
            if (!areTaskLeft()) {
                condition.signalAll();
            }
            return;
        }
        epochCompletion[current.getEpoch()]++;
        for (ProcessorTask<T> task : current.submit(graph, result)) {
            queue.add(task);
            condition.signal();
        }
        if (!areTaskLeft()) {
            condition.signalAll();
        }
    }

    @Override
    public boolean isTaskAvailable() {
        while (queue.size() > 0 && queue.peek().getEpoch() > lastEpoch) {
            queue.remove();
        }
        return queue.size() > 0 && areTaskLeft();
    }

    @Override
    public boolean areTaskLeft() {
        return !isExcepted() && lastEpoch != 0 && epochCompletion[lastEpoch - 1] != graph.size();
    }

    @Override
    public void error(ProcessorException e) {
        exception = e;
        condition.signalAll();
    }

    @Override
    public boolean isExcepted() {
        return exception != null;
    }

    @Override
    public ProcessorException getException() {
        return exception;
    }

    @Override
    public Map<String, List<T>> submit() {
        return graph.results(lastEpoch);
    }
}
