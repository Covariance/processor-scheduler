package processors;

import ru.covariance.jbintern.Processor;
import ru.covariance.jbintern.ProcessorException;

import java.util.List;

public class SleepingProcessor<T> implements Processor<T> {
    private final String ID;
    private final List<String> inputIDs;
    private final T result;
    private final int sleep;

    public SleepingProcessor(String ID, List<String> inputIDs, T result, int sleep) {
        this.ID = ID;
        this.inputIDs = inputIDs;
        this.result = result;
        this.sleep = sleep;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<String> getInputIds() {
        return inputIDs;
    }

    @Override
    public T process(List<T> input) throws ProcessorException {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            throw new ProcessorException("Sleeping processor interrupted");
        }
        return result;
    }
}
