package ru.covariance.jbintern.structure;

import ru.covariance.jbintern.ProcessorException;

@FunctionalInterface
public interface FedProcessor<T> {
    T process() throws ProcessorException;
}
