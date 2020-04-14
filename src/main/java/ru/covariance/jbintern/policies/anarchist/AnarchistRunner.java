package ru.covariance.jbintern.policies.anarchist;

import ru.covariance.jbintern.Processor;
import ru.covariance.jbintern.Runner;
import ru.covariance.jbintern.queue.confident.ProcessorGraph;
import ru.covariance.jbintern.queue.confident.ConfidentTaskQueue;
import ru.covariance.jbintern.queue.TaskQueue;
import ru.covariance.jbintern.ProcessorException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AnarchistRunner<T> implements Runner<T> {
    @Override
    public Map<String, List<T>> runProcessors(Set<Processor<T>> processors, int maxThreads, int maxIterations) throws ProcessorException {
        ProcessorGraph<T> graph = new ProcessorGraph<>(processors, maxIterations);
        TaskQueue<T> tq = new ConfidentTaskQueue<>(graph, maxIterations);
        if (maxThreads > processors.size() + 1) {
            maxThreads = processors.size() - 1;
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
