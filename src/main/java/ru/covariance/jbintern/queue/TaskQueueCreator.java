package ru.covariance.jbintern.queue;

import ru.covariance.jbintern.Processor;
import ru.covariance.jbintern.ProcessorException;

import java.util.Set;

public interface TaskQueueCreator<T> {
    TaskQueue<T> create(Set<Processor<T>> processors, int maxIterations, int maxThreads) throws ProcessorException;
}
