package ru.covariance.jbintern.queue.iterative;

import ru.covariance.jbintern.Processor;
import ru.covariance.jbintern.ProcessorException;
import ru.covariance.jbintern.queue.TaskQueue;
import ru.covariance.jbintern.structure.FedProcessor;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IterativeTaskQueue<T> implements TaskQueue<T> {
    private final Lock lock = new ReentrantLock(true);
    private final Condition condition = lock.newCondition();
    private final Queue<String> tasks = new ArrayDeque<>();
    private final Map<Thread, String> threadToID = new HashMap<>();
    private int epochCompletion = 0;

    private final ProcessorGraph<T> graph;
    private int epochsLeft;
    private boolean areTasksLeft = true;
    private ProcessorException exception = null;

    public static <T> IterativeTaskQueue<T> create(Set<Processor<T>> processors,
                                                   int maxIterations,
                                                   int maxThreads) throws ProcessorException {
        return new IterativeTaskQueue<>(processors, maxIterations, maxThreads);
    }

    public IterativeTaskQueue(Set<Processor<T>> processors, int maxIterations, int maxThreads) throws ProcessorException {
        this.graph = new ProcessorGraph<>(processors);
        this.epochsLeft = maxIterations;
        tasks.addAll(graph.initialTasks());
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
        if (tasks.size() == 0) {
            throw new IllegalStateException("No tasks to execute!");
        }
        threadToID.put(ID, tasks.element());
        return graph.feed(tasks.remove());
    }

    @Override
    public void submitTask(Thread ID, T result) {
        epochCompletion++;
        if (result == null) {
            condition.signalAll();
            areTasksLeft = false;
            return;
        }
        String task = threadToID.get(ID);
        List<String> addition = graph.submitTask(task, result);
        if (epochCompletion == graph.size()) {
            epochsLeft--;
            if (epochsLeft == 0) {
                condition.signalAll();
                areTasksLeft = false;
                return;
            }
            epochCompletion = 0;
            addition = graph.initialTasks();
        }
        for (String newTask : addition) {
            tasks.add(newTask);
            condition.signal();
        }
    }

    @Override
    public boolean isTaskAvailable() {
        return tasks.size() > 0 && areTasksLeft;
    }

    @Override
    public boolean areTaskLeft() {
        return areTasksLeft;
    }

    @Override
    public void error(ProcessorException exception) {
        this.exception = exception;
        areTasksLeft = false;
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
        return graph.getResults();
    }
}
