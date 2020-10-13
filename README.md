# Processor scheduler
## Goal
Implement the `Runner` interface satisfying the following contract:

```java
public interface Runner<T> {
    /**
     * Runs a set of interdependent processors many times until null is produced by any of them
     *
     * @param maxThreads    - maximum number of threads to be used
     * @param maxIterations - maximum number of iterations to run
     * @param processors    - a set of processors
     * @return a map, where the key is a processor id, and the value is a list of its outputs in the order of iterations
     * @throws ProcessorException if a processor throws an exception, loops detected, or some input ids not found
     */
    Map<String, List<T>> runProcessors(Set<Processor<T>> processors, int maxThreads, int maxIterations) throws ProcessorException;
}
```
`Processor` interface is specified as following:

```java
public interface Processor<T> {
    /**
     * @return processor id, immutable, unique among all instances, not null
     */
    String getId();

    /**
     * @return a list of processors that have to be executed before this one
     * and whose results must be passed to Processor::process,
     * immutable, can be null or empty, both means no inputs
     */
    List<String> getInputIds();

    /**
     * @param input outputs of the processors whose ids are returned by Processor::getInputIds, not null, but can be empty
     * @return output of the processing, null if no output is produced
     * @throws ProcessorException if error occurs during processing
     */
    T process(List<T> input) throws ProcessorException;
}
```
Following restrictions and rules must be followed:
- Task of `runProcessors` method is to run all set of processors in several iterations (not more than `maxIterations`) and return list of results of all iterations for each processor;
- During one iteration no processor can be run before all of its input processors on this iteration are finished;
- Processors that return empty input id lists are considered data sources and can be run ;
- Processors can be run using several threads, though not more than `maxThreads`;
- On each iteration ech processor can be run no more than once;
- More than one iteration can be run at a time, though sequence of iterations for each individual processor must be satisfied, which means that processor must be finished in a previous iteration to be run in the next one;
- If any processor throws an exception, all other threads must finish their work and `runProcessors` must also throw an exception;
- Also `runProcessors` must throw an exception if dependency graph has cycles or contains unknown input ids;
- If any processor returns `null`, results of that and all of the next iterations must be ignored, and `runProcessors` must return results of all previous iterations.

## Theory
Following implementation can be divided in three stages:

### Preprocessing
Preprocessing stage translates `Set<Processor<T>>` into more convinient form and checks if dependency graph has cycles or uknown input ids. 

#### Acyclic
Dependency graph can be cycle-checked using single DFS. All vertices are painted *black*,
when DFS enters the vertice, in paints it *gray*, and when it leaves vertice, it paints it *white*.
If DFS enters *gray* vertice, than graph has cycles, because there exists path from that vertice to itself.

### Scheduler creation
Scheduler, or *task queue*, will be the object/process, that gives task to executors,
saves their results, monitors the exceptions and `null` results. To describe scheduler
contracts I developed `TaskQueue` interface, and in order to pass it to the executor I 
created `TaskQueueCreator` interface. I have designed two different task queues:

#### [Confident task queue](src/main/java/ru/covariance/processorScheduler/queue/confident)
All processors are added to the task queue as soon as we're able to launch them:
- During queue initialization, if this processor is data source;
- After processor finishes its execution during previous iteration;
- After all input processors for this one during this iteration finish.

##### Pros:
 * Queue is always filled to its extent, and idles can occur only through unoptimal execution order.
##### Cons:
 * If some processor throws exception or returns null, we may have done too much redundant work.
 
#### [Iterative task queue](src/main/java/ru/covariance/processorScheduler/queue/iterative)
Iterations are processed sequentially, one at a time. Processors are added
to the queue only if:
- They are data sources and previous iteration have finished, or this iteration is first;
- After all input processors for this one during this iteration finish.

##### Pros:
 * Redundant work is minimized in case of `null` result or exception.

##### Cons:
 * It's not guaranteed that queue is always filled to its extent, and some executors may wait for tasks. 
 If there's a *bottleneck* in dependency graph, idle time can be very big.
 
### Execution of processors
For processors execution I have designed three different policies, which efficiency differ drastically on
different input data.

#### Boss policy
One main thread distribute stasks between all others, possibly changing some internal priorities of executors
and tasks.

##### Pros:
 * If there're a lot of available threads, this policy will be very efficient, because it can use more advanced algorithm
 to distribute tasks and therefore speed up overall execution.
##### Cons:
 * If there're not so many threads available, this policy will be very unefficient, because it will take a lot of
overall executor time to distribute tasks;
 * Special implementation must be coded for the case of only one thread.

#### Leader policy
One main thread distributes tasks between others and then takes the task for itself, repeating this algorithm upon
its completion.

##### Pros:
 * Leader thread is not idle at any time, executing processors as well as the others.
##### Cons:
 * Other threads must wait for leader's completion of its processor, possibly creating a lot of idles.
 
#### [Anarchist policy](src/main/java/ru/covariance/processorScheduler/policies/anarchist)
One of the threads starts all others and submits the results, but overall threads are equal, taking tasks from
sycnhronized queue object.

##### Pros:
 * Threads are idle if and only if there're no new tasks in queue; 
 * There's no main executor, which perfomance impacts overall runner perfomance.
##### Cons:
 * If there're a lot of simple tasks and many available threads, it can become much less effective, because most of the time 
 executors would just wait to synchronize on task queue.
##### Possible upgrades:
 * The only idle time of this runner is the time when executors queue to synchronize on queue. We can decrease this time
    by implementing some of these ideas:
    - Inspired by cache coherency protocols, we can do several instances of queue, that are synchronizing between each other.
    - We can split processor graph in several independent parts and process each one on its own.
 * It can happen that inner task queue ordering is unoptimal, and artificial *bottlenecks* appear. Inspired by *superscalar*
    architecture of conveyor executing we can somehow optimize inner task ordering based on results and perfomance of previous iterations.
    
## Implementation and testing
All beforementioned task queues implementes (labels are clickable).
Anarchist policy implemented.

A lot of testing done. You can read about them [here](src/test/java/README.md).

Tests are launched using `maven-surefire-plugin` with a `mvn test` command.
