import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import processors.SleepingProcessor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(Parameterized.class)
public class RunnerSimpleTest {
    @Parameterized.Parameters
    public static List<IntegerRunnerCreator> data() {
        return IntegerRunnerCreator.creators;
    }

    @Parameterized.Parameter
    public IntegerRunnerCreator creator;

    private void runnerSimpleTest(int countOfProcessors, int iterations, int threads, int sleep) {
        new TestCase<>(
                IntStream.range(0, countOfProcessors)
                        .mapToObj(i -> new SleepingProcessor<>(Integer.toHexString(i), List.of(), 0, sleep))
                        .collect(Collectors.toSet()),
                IntStream.range(0, countOfProcessors).boxed().collect(
                        Collectors.toMap(
                                Integer::toHexString,
                                i -> IntStream.generate(() -> 0).limit(iterations).boxed().collect(Collectors.toList())
                        )
                ),
                iterations
        ).test(creator.create(), threads);
    }

    @Test
    public void soloSimpleTest() {
        runnerSimpleTest(10, 10, 0, 10);
    }

    @Test
    public void smallSimpleTest() {
        runnerSimpleTest(1, 1, 1, 10);
    }

    @Test
    public void mediumSimpleTest() {
        IntStream.range(1, 100).forEach(i -> runnerSimpleTest(i, 10, 10, 1));
    }

    @Test
    public void bigSimpleTest() {
        IntStream.range(1, 100).forEach(i -> runnerSimpleTest(100 * i, 10, 100, 0));
    }
}
