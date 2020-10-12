import ru.covariance.processorScheduler.Runner;
import ru.covariance.processorScheduler.policies.anarchist.AnarchistRunner;
import ru.covariance.processorScheduler.queue.confident.ConfidentTaskQueue;
import ru.covariance.processorScheduler.queue.iterative.IterativeTaskQueue;

import java.util.List;

public interface IntegerRunnerCreator {
    Runner<Integer> create();

    List<IntegerRunnerCreator> creators = List.of(
            () -> new AnarchistRunner<>(ConfidentTaskQueue::create), // Anarchist with confident task queue
            () -> new AnarchistRunner<>(IterativeTaskQueue::create)  // Anarchist with iterative task queue
    );
}
