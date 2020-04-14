import ru.covariance.jbintern.Runner;
import ru.covariance.jbintern.policies.anarchist.AnarchistRunner;
import ru.covariance.jbintern.queue.confident.ConfidentTaskQueue;
import ru.covariance.jbintern.queue.iterative.IterativeTaskQueue;

import java.util.List;

public interface IntegerRunnerCreator {
    Runner<Integer> create();

    List<IntegerRunnerCreator> creators = List.of(
            () -> new AnarchistRunner<>(ConfidentTaskQueue::create), // Anarchist with confident task queue
            () -> new AnarchistRunner<>(IterativeTaskQueue::create)  // Anarchist with iterative task queue
    );
}
