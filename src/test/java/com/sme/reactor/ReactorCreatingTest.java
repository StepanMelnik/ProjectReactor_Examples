package com.sme.reactor;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Unit tests to work with all consumers of subscriber.
 */
public class ReactorCreatingTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactorCreatingTest.class);

    @Test
    public void testSubscribeWithAllConsumers()
    {
        List<String> result = new ArrayList<>();

        Disposable disposable = Flux.just("Hello ", "reactive ", "world!", ".")
                .filter(value -> value.length() > 1)
                .map(String::toUpperCase)
                .subscribe(value ->
                {
                    LOGGER.debug("Got {} value", value);
                    result.add(value);
                },
                        t ->
                        {
                            LOGGER.error("Got {} error", t);
                        },
                        () ->
                        {
                            LOGGER.debug("Completed callback");
                        });

        LOGGER.debug("It's disposable: {}", disposable.isDisposed());
        assertEquals(Arrays.asList("HELLO ", "REACTIVE ", "WORLD!"), result);
    }

    @Test
    public void testSubscribeWithErrorConsumer()
    {
        List<String> result = new ArrayList<>();

        Flux.just("hello ", "reactive ", null, "world", ".")  // null value
                .filter(value -> value.length() > 1)
                .subscribe(value ->
                {
                    LOGGER.debug("Got {} value", value);
                    result.add(value);
                },
                        t ->
                        {
                            LOGGER.error("Got {} error", t);
                        },
                        () ->
                        {
                            LOGGER.debug("Completed callback");
                        });

        assertEquals(Arrays.asList("hello ", "reactive "), result);
    }

    /**
     * Create Interval Flux that is performed asynchronously in the scheduler.
     * <p>
     * This Flux never finishes to emit, so we should unsubscribe the Disposable by subscribe.dispose().
     * </p>
     */
    @Test
    public void testInterval() throws InterruptedException
    {
        Disposable interval = Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
                .map(value ->
                {
                    LOGGER.debug("Increase value in {} thread", Thread.currentThread().getName());
                    return value * 2;
                })
                .subscribe(value -> System.out.println("Interval value:" + value));

        Thread.sleep(10000); // sleep here, because Interval never finishes
        interval.dispose(); // unscubscribe

        assertTrue(interval.isDisposed(), "Expects disposed Flux");
    }

    @Test
    public void testFluxFromStream()
    {
        Stream<String> stream = asList("!", "hello ", "reactive ", "reactive ", "world", "!")
                .stream()
                .map(String::toUpperCase)
                .distinct();

        List<String> result = new ArrayList<>();

        Flux.fromStream(stream)
                .doOnNext(value -> LOGGER.debug("Got {} value in {} thread", Thread.currentThread().getName()))
                .subscribe(result::add);

        assertEquals(asList("!", "HELLO ", "REACTIVE ", "WORLD"), result);
    }

    @Test
    public void testPipelineOnErrorResume() throws InterruptedException
    {
        CountDownLatch callingThreadBlocker = new CountDownLatch(1);
        // BlockingQueue<String> queue = new LinkedBlockingDeque<>();
        List<String> list = new ArrayList<>();

        Flux.just("hello ", "reactive ", null, "world!", "what?")
                .filter(w -> !w.equals("what?"))                // Filter "what?"
                .delayElements(Duration.ofMillis(2000))          // Signals to delay in parallel scheduler
                .map(w -> w.toUpperCase())
                //.onErrorReturn("Empty")                       // Returns empty if error occurs
                .onErrorResume(throwable ->                     // Error handling
                {
                    return Mono.just("Error because:" + throwable);
                })
                .subscribe(value ->
                {
                    LOGGER.debug("Got {} value", value);
                    list.add(value);
                },
                        t ->
                        {
                            LOGGER.error("Got {} error", t);
                        },
                        () ->
                        {
                            callingThreadBlocker.countDown();
                            LOGGER.debug("Completed callback");
                        });

        callingThreadBlocker.await();
        assertEquals(asList("HELLO ", "REACTIVE ", "Error because:java.lang.NullPointerException: Array returned null value"), list);
    }
}
