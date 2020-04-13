package ru.covariance.jbintern.policies.anarchist;

import ru.covariance.jbintern.structure.FedProcessor;
import ru.covariance.jbintern.queue.TaskQueue;
import ru.covariance.jbintern.ProcessorException;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class AnarchistWorker<T> implements Runnable {
    private final TaskQueue<T> taskQueue;
    private final Lock lock;
    private final Condition condition;

    public AnarchistWorker(TaskQueue<T> taskQueue, Lock lock, Condition condition) {
        this.taskQueue = taskQueue;
        this.lock = lock;
        this.condition = condition;
    }

    private FedProcessor<T> getTask() {
        lock.lock();
        while (!taskQueue.isTaskAvailable()) {
            if (!taskQueue.areTaskLeft()) {
                lock.unlock();
                return null;
            }
            try {
                condition.await();
            } catch (InterruptedException e) {
                System.err.println("Anarchist worker interrupted while waiting for the task: " + e.getMessage());
                lock.unlock();
                return null;
            }
        }
        FedProcessor<T> task = taskQueue.getTask(Thread.currentThread());
        lock.unlock();
        return task;
    }

    private void submitTask(T result) {
        lock.lock();
        taskQueue.submitTask(Thread.currentThread(), result);
        lock.unlock();
    }

    @Override
    public void run() {
        FedProcessor<T> task = getTask();
        if (task == null) return;
        while (true) {
            try {
                T result = task.process();
                submitTask(result);
            } catch (ProcessorException e) {
                lock.lock();
                taskQueue.error(e);
                lock.unlock();
            }
            task = getTask();
            if (task == null) return;
        }
    }
}
