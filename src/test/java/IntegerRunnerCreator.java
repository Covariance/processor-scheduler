import ru.covariance.jbintern.Runner;
import ru.covariance.jbintern.policies.anarchist.AnarchistRunner;

import java.util.List;

public interface IntegerRunnerCreator {
    Runner<Integer> create();

    List<IntegerRunnerCreator> creators = List.of(
            AnarchistRunner::new,
            AnarchistRunner::new
    );
}
