package processors;

import ru.covariance.jbintern.ProcessorException;

import java.util.List;

public class HeavyCalcProcessor extends AbstractTestProcessor {
    private final int iter;

    public HeavyCalcProcessor(String ID, List<String> inputIDs, int iter) {
        super(ID, inputIDs);
        this.iter = iter;
    }

    @Override
    public Integer process(List<Integer> input) throws ProcessorException {
        double a = iter;
        for (int i = 0; i < iter; i++) {
            a = Math.sin(a);
        }
        return 0;
    }
}
