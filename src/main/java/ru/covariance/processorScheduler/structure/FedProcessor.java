package ru.covariance.processorScheduler.structure;

import ru.covariance.processorScheduler.ProcessorException;

@FunctionalInterface
public interface FedProcessor<T> {
    T process() throws ProcessorException;
}
