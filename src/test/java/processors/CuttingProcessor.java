package processors;

import ru.covariance.processorScheduler.ProcessorException;

import java.util.List;

public class CuttingProcessor extends AbstractTestProcessor {
    private int untilCutting;

    public CuttingProcessor(String ID, List<String> inputIDs, int untilCutting) {
        super(ID, inputIDs);
        this.untilCutting = untilCutting;
    }

    @Override
    public Integer process(List<Integer> input) throws ProcessorException {
        return (--untilCutting == 0) ? null : 0;
    }
}
