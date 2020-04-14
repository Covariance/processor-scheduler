package processors;

import ru.covariance.jbintern.ProcessorException;

import java.util.List;

public class ExceptionalProcessor extends AbstractTestProcessor {
    private int untilException;

    public ExceptionalProcessor(String ID, List<String> inputIDs, int untilException) {
        super(ID, inputIDs);
        this.untilException = untilException;
    }

    @Override
    public Integer process(List<Integer> input) throws ProcessorException {
        if (--untilException == 0) {
            throw new ProcessorException("it's time to expect unexpected");
        }
        return 0;
    }
}
