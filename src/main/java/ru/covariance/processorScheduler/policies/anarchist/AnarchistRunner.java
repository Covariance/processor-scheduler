package ru.covariance.processorScheduler.policies.anarchist;

import ru.covariance.processorScheduler.Processor;
import ru.covariance.processorScheduler.Runner;
import ru.covariance.processorScheduler.queue.TaskQueueCreator;
import ru.covariance.processorScheduler.queue.TaskQueue;
import ru.covariance.processorScheduler.ProcessorException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AnarchistRunner<T> implements Runner<T> {
    private final TaskQueueCreator<T> tqc;

    public AnarchistRunner(TaskQueueCreator<T> tqc) {
        this.tqc = tqc;
    }

    @Override
    public Map<String, List<T>> runProcessors(Set<Processor<T>> processors, int maxThreads, int maxIterations) throws ProcessorException {
        TaskQueue<T> tq = tqc.create(processors, maxIterations, maxThreads);
        if (maxThreads > processors.size() + 1) {
            maxThreads = processors.size();
        }
        Lock lock = tq.getLock();
        Condition condition = tq.getCondition();
        lock.lock();
        List<Thread> workers = IntStream.range(0, maxThreads)
                .mapToObj(i -> new Thread(new AnarchistWorker<>(tq, lock, condition)))
                .collect(Collectors.toList());
        workers.forEach(Thread::start);
        lock.unlock();
        new AnarchistWorker<>(tq, lock, condition).run();
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                System.err.println("Submitter interrupted while waiting for other threads to die: " + e.getMessage());
            }
        }
        if (tq.isExcepted()) {
            throw tq.getException();
        }
        return tq.submit();
    }
}
