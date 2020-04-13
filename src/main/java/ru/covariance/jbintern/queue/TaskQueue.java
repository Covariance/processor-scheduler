package ru.covariance.jbintern.queue;

import ru.covariance.jbintern.ProcessorException;
import ru.covariance.jbintern.structure.FedProcessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

public interface TaskQueue<T> {
    /**
     * Returns the lock associated with this TaskQueue. It must be guaranteed that
     * no operation is made with the queue until lock is locked, thus it is essential to
     * store this lock in order to not invoke this method when there's concurrency
     * for this task queue.
     * @return lock associated with this TaskQueue.
     */
    Lock getLock();

    /**
     * Returns condition associated with this TaskQueue.
     * @return condition associated with this TaskQueue.
     */
    Condition getCondition();

    /**
     * Returns task to complete from this task queue. Its inputs must be already fed in.
     * It is essential to invoke {@link #isTaskAvailable() isTaskAvailable} method before
     * invoking this method and to get true reply.
     * @param ID Thread that asks for a task
     * @return task to complete from this task queue.
     * @throws IllegalStateException if previously invoking {@link #isTaskAvailable() isTaskAvailable}
     * method would give false result.
     */
    FedProcessor<T> getTask(Thread ID) throws IllegalStateException;

    /**
     * Submit completed task to this task queue.
     *
     * If this operation produces new tasks in this queue, then it must
     * {@link Condition#signal() signal} on its condition at least the number of times
     * equal to the number of tasks produced, or {@link Condition#signalAll() signalAll},
     * though it's not recommended.
     *
     * If this method submits the last task to this queue, then it must
     * {@link #notifyAll()} on its lock.
     * @param ID thread that is submitting the task
     * @param result result of {@link Supplier#get() get} method of task
     */
    void submitTask(Thread ID, T result);

    /**
     * Returns true only if there are currently available task
     * @return true only if there are currently available task
     */
    boolean isTaskAvailable();

    /**
     * Returns false only if there would be no tasks in this queue anymore
     * @return false only if there would be no tasks in this queue anymore
     */
    boolean areTaskLeft();

    /**
     * Tells TaskQueue that an error has occurred during executing of a task
     * @param exception exact exception that occurred
     */
    void error(ProcessorException exception);

    /**
     * Returns true only if exception occurred during processing.
     * @return true only if exception occurred during processing
     */
    boolean isExcepted();

    /**
     * Returns the exception that occurred during processing or null
     * if it didn't happen.
     * @return the exception that occurred during processing or null
     * if it didn't happen
     */
    ProcessorException getException();

    /**
     * Method that submits the result of execution of processes in the task queue.
     * It must only be called when {@link #areTaskLeft() areTasksLeft} method and
     * {@link #isExcepted() isExcepted} method both returned false.
     * @return a map, where the key is a processor id, and the value
     * is a list of its outputs in the order of iterations
     */
    Map<String, List<T>> submit();
}
