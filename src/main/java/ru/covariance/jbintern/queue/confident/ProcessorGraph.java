package ru.covariance.jbintern.queue.confident;

import ru.covariance.jbintern.Processor;
import ru.covariance.jbintern.ProcessorException;
import ru.covariance.jbintern.structure.FedProcessor;
import ru.covariance.jbintern.structure.UnfedProcessor;

import java.util.*;
import java.util.stream.Collectors;

public class ProcessorGraph<T> {
    private final Map<String, ProcessorNode<T>> lookup = new HashMap<>();
    private final int epochCnt;

    private class ProcessorNode<R> {
        public boolean isExecuting = false;
        public int currentEpoch = 0;
        public final String ID;
        public final int[] epochInputsCompletion;
        public final UnfedProcessor<R> processor;
        public final List<R> results = new ArrayList<>();
        public final List<ProcessorNode<R>> inputs = new ArrayList<>();
        public final List<ProcessorNode<R>> outputs = new ArrayList<>();

        public ProcessorNode(String ID, UnfedProcessor<R> processor) {
            this.ID = ID;
            this.processor = processor;
            epochInputsCompletion = new int[epochCnt];
        }

        private ProcessorTask<R> getNewTask() {
            isExecuting = true;
            return new ProcessorTask<>(currentEpoch, ID);
        }

        public List<ProcessorTask<R>> submit(R result) {
            results.add(result);
            isExecuting = false;
            List<ProcessorTask<R>> newTasks = new ArrayList<>();
            for (ProcessorNode<R> output : outputs) {
                output.epochInputsCompletion[currentEpoch]++;
                if (!output.isExecuting && output.epochInputsCompletion[currentEpoch] == output.inputs.size()) {
                    newTasks.add(output.getNewTask());
                }
            }
            if (++currentEpoch != epochCnt && epochInputsCompletion[currentEpoch] == inputs.size()) {
                newTasks.add(this.getNewTask());
            }
            return newTasks;
        }

        public FedProcessor<R> feed() {
            final List<R> input = inputs.stream().map((i) -> (i.results.get(currentEpoch))).collect(Collectors.toList());
            return () -> processor.process(input);
        }
    }

    public ProcessorGraph(Set<Processor<T>> processorSet, int epochCnt) throws ProcessorException {
        this.epochCnt = epochCnt;
        for (Processor<T> processor : processorSet) {
            lookup.put(processor.getId(), new ProcessorNode<>(processor.getId(), processor::process));
        }
        for (Processor<T> processor : processorSet) {
            ProcessorNode<T> a = lookup.get(processor.getId());
            List<String> inputs = processor.getInputIds();
            if (inputs == null) {
                inputs = List.of();
            }
            for (String input : inputs) {
                ProcessorNode<T> b = lookup.get(input);
                if (b == null) {
                    throw new ProcessorException("Processor dependency graph has unknown inputs: " + input);
                }
                a.inputs.add(b);
                b.outputs.add(a);
            }
        }
        if (!acyclicCheck()) {
            throw new ProcessorException("Processor dependency graph has cycles.");
        }
    }

    // region acyclic
    private boolean dfs(String v, Map<String, Integer> color) {
        color.put(v, 1);
        for (ProcessorNode<T> son : lookup.get(v).outputs) {
            if (color.get(son.ID) != 2 && (color.get(son.ID) == 1 || dfs(son.ID, color))) {
                return true;
            }
        }
        color.put(v, 2);
        return false;
    }

    private boolean acyclicCheck() {
        Map<String, Integer> color = new HashMap<>();
        for (String ID : lookup.keySet()) {
            color.put(ID, 0);
        }
        for (String ID : color.keySet()) {
            if (color.get(ID) == 0) {
                if (dfs(ID, color)) {
                    return false;
                }
            }
        }
        return true;
    }
    // endregion

    public Map<String, List<T>> results(int lastEpoch) {
        Map<String, List<T>> result = new HashMap<>();
        for (String ID : lookup.keySet()) {
            result.put(ID, lookup.get(ID).results.subList(0, lastEpoch));
        }
        return result;
    }

    public List<ProcessorTask<T>> initialTasks() {
        return lookup.values().stream()
                .filter(node -> node.inputs.size() == 0)
                .map(ProcessorNode::getNewTask)
                .collect(Collectors.toList()
                );
    }

    public int size() {
        return lookup.size();
    }

    public FedProcessor<T> feed(String node) {
        return lookup.get(node).feed();
    }

    public List<ProcessorTask<T>> submit(String node, T result) {
        return lookup.get(node).submit(result);
    }
}
