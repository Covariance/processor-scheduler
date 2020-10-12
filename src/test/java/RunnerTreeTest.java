import generators.Graph;
import generators.TreeGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.covariance.processorScheduler.Processor;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(Parameterized.class)
public class RunnerTreeTest {
    @Parameterized.Parameters
    public static List<IntegerRunnerCreator> data() {
        return IntegerRunnerCreator.creators;
    }

    @Parameterized.Parameter
    public IntegerRunnerCreator creator;

    private void dfs(int v, List<Integer> result, List<List<Integer>> edgeSet) {
        int sum = v;
        for (Integer son : edgeSet.get(v)) {
            if (result.get(son) == -1) {
                dfs(son, result, edgeSet);
            }
            sum += result.get(son);
        }
        result.set(v, sum);
    }

    private void treeTest(Random rand, int countOfProcessors, int iterations, int threads) {
        Graph g = TreeGenerator.generateRandomTree(rand, countOfProcessors);
        List<List<Integer>> edgeSet = g.getEdgeSet();
        List<Integer> result = IntStream.generate(() -> -1).boxed().limit(g.getN()).collect(Collectors.toList());
        for (int i = 0; i < g.getN(); i++) {
            if (result.get(i) == -1) {
                dfs(i, result, edgeSet);
            }
        }
        new TestCase<>(
                IntStream.range(0, countOfProcessors)
                        .mapToObj(i -> new Processor<Integer>() {
                                    @Override
                                    public String getId() {
                                        return Integer.toHexString(i);
                                    }

                                    @Override
                                    public List<String> getInputIds() {
                                        return edgeSet.get(i).stream()
                                                .map(Integer::toHexString)
                                                .collect(Collectors.toList());
                                    }

                                    @Override
                                    public Integer process(List<Integer> input) {
                                        return i + input.stream().mapToInt(i -> i).sum();
                                    }
                                }
                        ).collect(Collectors.toSet()),
                IntStream.range(0, countOfProcessors).boxed().collect(Collectors.toMap(
                        Integer::toHexString,
                        i -> IntStream.generate(() -> result.get(i)).limit(iterations).boxed().collect(Collectors.toList())
                )),
                iterations
        ).test(creator.create(), threads);
    }

    @Test
    public void soloTreeTest() {
        treeTest(new Random(), 100, 10, 0);
    }

    @Test
    public void smallTreeTest() {
        treeTest(new Random(), 100, 10, 10);
    }

    @Test
    public void mediumTreeTest() {
        IntStream.range(0, 10).forEach(i -> treeTest(new Random(), (i + 1) * 100, 100, 10));
    }

    @Test
    public void bigTreeTest() {
        IntStream.range(0, 10).forEach(i -> treeTest(new Random(), (i + 1) * 1000, 100, 10));
    }
}
