package ru.covariance.processorScheduler.queue;

import ru.covariance.processorScheduler.Processor;
import ru.covariance.processorScheduler.ProcessorException;

import java.util.Set;

public interface TaskQueueCreator<T> {
    TaskQueue<T> create(Set<Processor<T>> processors, int maxIterations, int maxThreads) throws ProcessorException;
}
