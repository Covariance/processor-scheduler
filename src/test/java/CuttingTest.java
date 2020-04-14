import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import processors.CuttingProcessor;
import processors.HeavyCalcProcessor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(Parameterized.class)
public class CuttingTest {
    @Parameterized.Parameters
    public static List<IntegerRunnerCreator> data() {
        return IntegerRunnerCreator.creators;
    }

    @Parameterized.Parameter
    public IntegerRunnerCreator creator;

    @Test
    public void halfCuttingTest() {
        new TestCase<>(
                Set.of(
                        new CuttingProcessor("one", List.of("two"), 5),
                        new HeavyCalcProcessor("two", List.of(), 1)
                ),
                Map.of(
                        "one", List.of(0, 0, 0, 0),
                        "two", List.of(0, 0, 0, 0)
                ),
                10
        ).test(creator.create(), 10);
    }

    @Test
    public void zeroCuttingTest() {
        new TestCase<>(
                Set.of(
                        new CuttingProcessor("one", List.of(), 1)
                ),
                Map.of(
                        "one", List.of()
                ),
                10
        ).test(creator.create(), 10);
    }
}
