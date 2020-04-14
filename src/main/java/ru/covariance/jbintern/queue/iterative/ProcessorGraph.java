package ru.covariance.jbintern.queue.iterative;

import ru.covariance.jbintern.Processor;
import ru.covariance.jbintern.ProcessorException;
import ru.covariance.jbintern.structure.FedProcessor;
import ru.covariance.jbintern.structure.UnfedProcessor;

import java.util.*;
import java.util.stream.Collectors;

public class ProcessorGraph<T> {
    private final Map<String, List<T>> results = new HashMap<>();
    private final Map<String, UnfedProcessor<T>> processors = new HashMap<>();
    private final Map<String, Integer> completedInputs = new HashMap<>();
    private final Map<String, T> lastEpoch = new HashMap<>();

    private final Map<String, List<String>> inputs;
    private final Map<String, List<String>> outputs;

    public ProcessorGraph(Set<Processor<T>> processors) throws ProcessorException {
        Set<String> lookup = processors.stream().map(Processor::getId).collect(Collectors.toCollection(HashSet::new));
        this.inputs = lookup.stream().collect(Collectors.toMap(i -> i, i -> new ArrayList<>()));
        this.outputs = lookup.stream().collect(Collectors.toMap(i -> i, i -> new ArrayList<>()));
        for (Processor<T> processor : processors) {
            String ID = processor.getId();
            this.results.put(ID, new ArrayList<>());
            this.processors.put(ID, processor::process);
            List<String> edges = processor.getInputIds();
            if (edges == null) {
                edges = List.of();
            }
            for (String edge : edges) {
                if (!lookup.contains(edge)) {
                    throw new ProcessorException("Processor dependency graph has unknown inputs: " + edge);
                }
                this.inputs.get(ID).add(edge);
                this.outputs.get(edge).add(ID);
            }
        }
        if (!acyclicCheck()) {
            throw new ProcessorException("Processor dependency graph has cycles.");
        }
    }

    // region acyclic
    private boolean dfs(String v, Map<String, Integer> color) {
        color.put(v, 1);
        for (String son : inputs.get(v)) {
            if (color.get(son) != 2 && (color.get(son) == 1 || dfs(son, color))) {
                return true;
            }
        }
        color.put(v, 2);
        return false;
    }

    private boolean acyclicCheck() {
        Map<String, Integer> color = new HashMap<>();
        for (String ID : inputs.keySet()) {
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

    public List<String> initialTasks() {
        return inputs.keySet().stream().filter(ID -> inputs.get(ID).size() == 0).collect(Collectors.toList());
    }

    public Map<String, List<T>> getResults() {
        return results;
    }

    public FedProcessor<T> feed(String ID) {
        return () -> processors.get(ID).process(
                inputs.get(ID).stream()
                        .map(lastEpoch::get)
                        .collect(Collectors.toList())
        );
    }


    public int size() {
        return inputs.size();
    }

    public List<String> submitTask(String task, T result) {
        lastEpoch.put(task, result);
        if (lastEpoch.size() == inputs.size()) {
            lastEpoch.keySet().forEach(ID -> results.get(ID).add(lastEpoch.get(ID)));
            lastEpoch.clear();
            completedInputs.clear();
            return List.of();
        }
        List<String> res = new ArrayList<>();
        for (String dependent : outputs.get(task)) {
            completedInputs.put(dependent, completedInputs.getOrDefault(dependent, 0) + 1);
            if (completedInputs.get(dependent) == inputs.get(dependent).size()) {
                res.add(dependent);
            }
        }
        return res;
    }
}
