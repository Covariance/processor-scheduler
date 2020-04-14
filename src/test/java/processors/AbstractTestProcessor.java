package processors;

import ru.covariance.jbintern.Processor;

import java.util.List;

public abstract class AbstractTestProcessor implements Processor<Integer> {
    protected final String ID;
    protected final List<String> inputIDs;

    public AbstractTestProcessor(String ID, List<String> inputIDs) {
        this.ID = ID;
        this.inputIDs = inputIDs;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<String> getInputIds() {
        return inputIDs;
    }
}
