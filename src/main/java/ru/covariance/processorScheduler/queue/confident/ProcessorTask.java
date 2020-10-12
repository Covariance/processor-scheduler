package ru.covariance.processorScheduler.queue.confident;

import ru.covariance.processorScheduler.structure.FedProcessor;

import java.util.List;

public class ProcessorTask<T> {
    private final int epoch;
    private final String node;

    public ProcessorTask(int epoch, String node) {
        this.epoch = epoch;
        this.node = node;
    }

    public int getEpoch() {
        return epoch;
    }

    public FedProcessor<T> feed(ProcessorGraph<T> graph) {
        return graph.feed(node);
    }

    public List<ProcessorTask<T>> submit(ProcessorGraph<T> graph, T result) {
        return graph.submit(node, result);
    }
}
