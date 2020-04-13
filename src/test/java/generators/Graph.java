package generators;

import ru.covariance.jbintern.Processor;
import ru.covariance.jbintern.ProcessorException;
import ru.covariance.jbintern.structure.UnfedProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Graph {
    private final int n;
    private final List<List<Integer>> edgeSet;

    public Graph(int n, List<List<Integer>> edgeSet) {
        this.n = n;
        this.edgeSet = edgeSet;
    }

    public <T> Set<Processor<T>> toProcessorSet(UnfedProcessor<T> function) {
        Set<Processor<T>> result = new HashSet<>();
        for (int i = 0; i < n; i++) {
            final int id = i;
            final List<String> input = edgeSet.get(id).stream().map(Integer::toHexString).collect(Collectors.toList());
            result.add(
                    new Processor<T>() {
                        @Override
                        public String getId() {
                            return Integer.toHexString(id);
                        }

                        @Override
                        public List<String> getInputIds() {
                            return input;
                        }

                        @Override
                        public T process(List<T> input) throws ProcessorException {
                            return function.process(input);
                        }
                    }
            );
        }
        return result;
    }
}
