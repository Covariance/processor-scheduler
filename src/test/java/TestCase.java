import org.junit.Assert;
import ru.covariance.processorScheduler.Processor;
import ru.covariance.processorScheduler.ProcessorException;
import ru.covariance.processorScheduler.Runner;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestCase<T> {
    private final Set<Processor<T>> input;
    private final int iterations;
    private final Map<String, List<T>> result;

    public TestCase(Set<Processor<T>> input, Map<String, List<T>> result, int iterations) {
        this.input = input;
        this.iterations = iterations;
        this.result = result;
    }

    public void test(Runner<T> runner, int maxThreads) {
        try {
            Assert.assertEquals(runner.runProcessors(
                    input,
                    maxThreads,
                    iterations
            ), result);
        } catch (ProcessorException e) {
            Assert.fail();
        }
    }
}
