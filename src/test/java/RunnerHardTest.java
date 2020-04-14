import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import processors.IncrementingProcessor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(Parameterized.class)
public class RunnerHardTest {
    @Parameterized.Parameters
    public static List<IntegerRunnerCreator> data() {
        return IntegerRunnerCreator.creators;
    }

    @Parameterized.Parameter
    public IntegerRunnerCreator creator;

    private void hardTest(int countOfProcessors, int iterations, int threads) {
        new TestCase<>(
                IntStream.range(0, countOfProcessors).mapToObj(
                        i -> new IncrementingProcessor(Integer.toHexString(i),
                                (i == 0) ? List.of() : List.of(Integer.toHexString(i - 1)))
                ).collect(Collectors.toSet()),
                IntStream.range(0, countOfProcessors).boxed().collect(Collectors.toMap(
                        Integer::toHexString,
                        i -> IntStream.generate(() -> i).limit(iterations).boxed().collect(Collectors.toList())
                )),
                iterations
        ).test(creator.create(), threads);
    }

    @Test
    public void soloHardTest() {
        hardTest(100, 10, 0);
    }

    @Test
    public void smallHardTest() {
        hardTest(100, 10, 10);
    }

    @Test
    public void mediumHardTest() {
        IntStream.range(0, 10).forEach(i -> hardTest((i + 1) * 100, 100, 10));
    }

    @Test
    public void bigHardTest() {
        IntStream.range(0, 10).forEach(i -> hardTest((i + 1) * 1000, 100, 10));
    }
}
