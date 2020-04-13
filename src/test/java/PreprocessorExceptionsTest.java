import org.junit.Test;
import ru.covariance.jbintern.ProcessorException;
import ru.covariance.jbintern.Runner;
import ru.covariance.jbintern.policies.anarchist.AnarchistRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PreprocessorExceptionsTest {
    @Test(expected = ProcessorException.class)
    public void cyclicTest() throws ProcessorException {
        Runner<String> runner = new AnarchistRunner<>();
        runner.runProcessors(
                Set.of(
                        new SleepingProcessor<>("one", List.of("two"), "oops", 10),
                        new SleepingProcessor<>("two", List.of("three"), "oops", 10),
                        new SleepingProcessor<>("three", List.of("one"), "oops", 10)
                ),
                10,
                10
        );
    }

    @Test(expected = ProcessorException.class)
    public void loopCyclicTest() throws ProcessorException {
        Runner<String> runner = new AnarchistRunner<>();
        runner.runProcessors(
                Set.of(
                        new SleepingProcessor<>("one", List.of("one"), "oops", 10)
                ),
                10,
                10
        );
    }

    @Test(expected = ProcessorException.class)
    public void bigCyclicTest() throws ProcessorException {
        Runner<String> runner = new AnarchistRunner<>();
        int bigCnt = 1000;
        runner.runProcessors(
                IntStream.range(0, bigCnt)
                        .mapToObj(i -> new SleepingProcessor<>(
                                        Integer.toHexString(i),
                                        List.of(Integer.toHexString((i + 1) % bigCnt)),
                                        "oops",
                                        1
                                )
                        ).collect(Collectors.toCollection(HashSet::new)),
                10,
                10
        );
    }

    @Test(expected = ProcessorException.class)
    public void unknownIDsTest() throws ProcessorException {
        Runner<String> runner = new AnarchistRunner<>();
        runner.runProcessors(
                Set.of(
                        new SleepingProcessor<>("one", List.of("two"), "oops", 10),
                        new SleepingProcessor<>("two", List.of(), "oops", 10),
                        new SleepingProcessor<>("three", List.of("four"), "oops", 10)
                ),
                10,
                10
        );
    }
}
