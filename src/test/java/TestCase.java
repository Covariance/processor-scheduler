import ru.covariance.jbintern.Processor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestCase<T> {
    private final Set<Processor<T>> input;
    private final int iterations;
    private final Map<String, List<T>> result;

    public TestCase(Set<Processor<T>> input, int iterations, Map<String, List<T>> result) {
        this.input = input;
        this.iterations = iterations;
        this.result = result;
    }
}
