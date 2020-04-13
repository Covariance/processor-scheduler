package ru.covariance.jbintern.structure;

import ru.covariance.jbintern.ProcessorException;

import java.util.List;

@FunctionalInterface
public interface UnfedProcessor<T> {
    T process(List<T> inputs) throws ProcessorException;
}
