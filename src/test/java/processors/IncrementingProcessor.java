package processors;

import ru.covariance.jbintern.ProcessorException;

import java.util.List;

public class IncrementingProcessor extends AbstractTestProcessor {

    public IncrementingProcessor(String ID, List<String> inputIDs) {
        super(ID, inputIDs);
    }

    @Override
    public Integer process(List<Integer> input) throws ProcessorException {
        if (input == null || input.size() == 0) {
            return 0;
        }
        return input.get(0) + 1;
    }
}
