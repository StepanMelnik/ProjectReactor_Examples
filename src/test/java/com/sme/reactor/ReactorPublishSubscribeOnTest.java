package com.sme.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * Unit tests to work with subscribeOn and publishOn schedulers.
 */
public class ReactorPublishSubscribeOnTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactorPublishSubscribeOnTest.class);
    private static final int TTL_SECONDS = 15;  // 15s

    /**
     * Create three Flux pipelines with subscribeOn own scheduler to run async all of them.
     */
    @Test
    public void testAsyncSubscribeOn()
    {
        Hooks.onOperatorDebug();

        Scheduler scheduler1 = Schedulers.newElastic("scheduler", TTL_SECONDS);
        Scheduler scheduler2 = Schedulers.newElastic("scheduler1", TTL_SECONDS);
        Scheduler scheduler3 = Schedulers.newElastic("scheduler2", TTL_SECONDS);

        Flux<String> flux1 = Flux.just("Hey reactive ")
                .doOnNext(value ->
                {
                    LOGGER.debug("Flux1: Value {} on {} thread", value, Thread.currentThread().getName());
                })
                .subscribeOn(scheduler1);

        Flux<String> flux2 = Flux.just("flux ")
                .doOnNext(value ->
                {
                    LOGGER.debug("Flux2: Value {} on {} thread", value, Thread.currentThread().getName());
                })
                .subscribeOn(scheduler2);

        Flux<String> flux3 = Flux.just("world!")
                .doOnNext(value ->
                {
                    LOGGER.debug("Flux3: Value {} on {} thread", value, Thread.currentThread().getName());
                })
                .subscribeOn(scheduler3);

        AtomicReference<String> stringReference = new AtomicReference<>();
        Flux<Tuple3<String, String, String>> zipFlux = Flux.zip(flux1, flux2, flux3);
        FirstStep<Tuple3<String, String, String>> stepVerifier = StepVerifier.create(zipFlux);

        zipFlux
                .map(tuple3 ->
                {
                    return tuple3.getT1().concat(tuple3.getT2()).concat(tuple3.getT3());
                })
                .map(String::toUpperCase)
                .subscribe(value ->
                {
                    LOGGER.debug("Result:" + value);
                    stringReference.set(value);
                });

        stepVerifier.expectNext(Tuples.of("Hey reactive ", "flux ", "world!"))
                .expectComplete()
                .verify();

        assertEquals("HEY REACTIVE FLUX WORLD!", stringReference.get());
    }

    /**
     * PublishOn operator makes the pipeline run asynchronously in new scheduler.
     * <p>
     * It means that all steps will be executed in main thread before publishOn operator. All other steps will be performed in the scheduler thread.
     * </p>
     */
    @Test
    public void testPublishOn()
    {
        Scheduler scheduler = Schedulers.newElastic("scheduler");
        CountDownLatch callingThreadBlocker = new CountDownLatch(1);
        AtomicReference<String> reference = new AtomicReference<>();

        Flux<String> flux = Flux.just("Hello ", "async ", "world");
        FirstStep<String> stepVerifier = StepVerifier.create(flux);

        flux
                .doOnNext(value ->
                {
                    LOGGER.debug("{} current thread", Thread.currentThread().getName());
                })
                .publishOn(scheduler)
                .doOnNext(value ->
                {
                    LOGGER.debug("{} thread after publishOn", Thread.currentThread().getName());
                })
                .doOnComplete(() ->
                {
                    LOGGER.debug("Completed");
                    callingThreadBlocker.countDown();
                    ;
                })
                .subscribe(value ->
                {
                    LOGGER.debug("Result:" + value);
                    reference.set(reference.get() == null ? value : reference.get() + value);
                });

        stepVerifier.expectNext("Hello ")
                .expectNext("async ")
                .expectNext("world")
                .expectComplete()
                .verify();

        try
        {
            callingThreadBlocker.await();
        }
        catch (InterruptedException e)
        {
            // ignore
        }

        assertEquals("Hello async world", reference.get());
    }
}
