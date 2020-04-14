import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import processors.ExceptionalProcessor;
import processors.HeavyCalcProcessor;
import processors.SleepingProcessor;
import ru.covariance.jbintern.ProcessorException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(Parameterized.class)
public class ExceptionsTest {
    @Parameterized.Parameters
    public static List<IntegerRunnerCreator> data() {
        return IntegerRunnerCreator.creators;
    }

    @Parameterized.Parameter
    public IntegerRunnerCreator creator;

    @Test(expected = ProcessorException.class)
    public void cyclicTest() throws ProcessorException {
        creator.create().runProcessors(
                Set.of(
                        new SleepingProcessor<>("one", List.of("two"), -1, 10),
                        new SleepingProcessor<>("two", List.of("three"), -1, 10),
                        new SleepingProcessor<>("three", List.of("one"), -1, 10)
                ),
                10,
                10
        );
    }

    @Test(expected = ProcessorException.class)
    public void loopCyclicTest() throws ProcessorException {
        creator.create().runProcessors(
                Set.of(
                        new SleepingProcessor<>("one", List.of("one"), -1, 10)
                ),
                10,
                10
        );
    }

    @Test(expected = ProcessorException.class)
    public void bigCyclicTest() throws ProcessorException {
        int bigCnt = 1000;
        creator.create().runProcessors(
                IntStream.range(0, bigCnt)
                        .mapToObj(i -> new SleepingProcessor<>(
                                        Integer.toHexString(i),
                                        List.of(Integer.toHexString((i + 1) % bigCnt)),
                                        -1,
                                        1
                                )
                        ).collect(Collectors.toCollection(HashSet::new)),
                10,
                10
        );
    }

    @Test(expected = ProcessorException.class)
    public void unknownIDsTest() throws ProcessorException {
        creator.create().runProcessors(
                Set.of(
                        new SleepingProcessor<>("one", List.of("two"), -1, 10),
                        new SleepingProcessor<>("two", List.of(), -1, 10),
                        new SleepingProcessor<>("three", List.of("four"), -1, 10)
                ),
                10,
                10
        );
    }

    @Test(expected = ProcessorException.class)
    public void exceptionInProcessingTest() throws ProcessorException {
        creator.create().runProcessors(
                Set.of(
                        new ExceptionalProcessor("one", List.of("two"), 10),
                        new HeavyCalcProcessor("two", List.of("three"), 100),
                        new SleepingProcessor<>("three", List.of(), 0, 1)
                ),
                10,
                20
        );
    }

    @Test(expected = ProcessorException.class)
    public void competingExceptionInProcessingTest() throws ProcessorException {
        creator.create().runProcessors(
                Set.of(
                        new ExceptionalProcessor("one", List.of(), 2),
                        new ExceptionalProcessor("two", List.of(), 2)
                ),
                10,
                10
        );
    }
}
