package ru.covariance.processorScheduler.structure;

import ru.covariance.processorScheduler.ProcessorException;

import java.util.List;

@FunctionalInterface
public interface UnfedProcessor<T> {
    T process(List<T> inputs) throws ProcessorException;
}
