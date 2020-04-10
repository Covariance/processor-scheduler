# Processors scheduler
## Условия:
Требуется написать реализацию интерфейса `Runner`, удовлетворяющую контракту интерфейса:

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

Реализация должна удовлетворять следующим требованиям:
- Задача метода `runProcessors` – запускать весь набор «процессоров» в несколько итераций (не более `maxIterations`)
 и возвратить список результатов каждого для всех полных итераций;
- В рамках каждой итерации ни один «процессор» не запускается пока не будут
 запущены все те, что соответствуют его `input ids`;
- Некоторые «процессоры» возвращают пустые списки `input ids`,
 они являются источником данных и могут запускаться сразу;
- «Процессоры» могут (и должны) выполняться в несколько потоков, но не более чем `maxThreads`;
- В каждой итерации каждый «процессор» запускается ровно один раз;
- Может запускаться больше одной итерации одновременно, но ни один «процессор»
 не может запускаться параллельно самому себе;
- Последовательность итераций для каждого «процессора» должна соблюдаться, то есть он не может
 быть запущен в итерации, если еще не завершился в предыдущей;
- Если хоть один «процессор» кидает исключение, все остальные потоки должны прерываться
 и `runProcessors` тоже должен кидать исключение;
- Также `runProcessors` должен кидать исключение, если граф зависимостей содержит циклы или неизвестные `input ids`
- Если хоть один «процессор» возвращает `null`, результаты этой и всех последующих (если они уже запущены)
 итераций должны игнорироваться и `runProcessors` должен возвращать результат всех предыдущих итераций;

