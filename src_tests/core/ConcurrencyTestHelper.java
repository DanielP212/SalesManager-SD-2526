package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ConcurrencyTestHelper {
    /**
     * Runs the given runnable task concurrently in N threads.
     * Fails the test if any thread throws an exception or if the timeout is reached.
     */
    public static void runConcurrently(int numberOfThreads, Runnable task) throws InterruptedException {
        runConcurrently(numberOfThreads, task, 10);
    }

    public static void runConcurrently(int numberOfThreads, Runnable task, int timeoutSeconds) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                try {
                    startLatch.await();
                    task.run();
                } catch (Throwable t) {
                    exceptions.add(t);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean allFinished = endLatch.await(timeoutSeconds, TimeUnit.SECONDS);

        service.shutdownNow();

        assertTrue(allFinished, "Test timed out - potential deadlock or slow performance");

        if (!exceptions.isEmpty()) {
            Throwable firstException = exceptions.get(0);
            fail("Thread failed with exception: " + firstException.getMessage(), firstException);
        }
    }
}
