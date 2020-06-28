package com.sme.reactor;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.SignalType;

/**
 * Unit tests to work with BackPressure based on a limit of requests in subscriber.
 */
public class ReactorBackpressureTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactorBackpressureTest.class);

    @BeforeEach
    public void setUp()
    {
        Hooks.onOperatorDebug();
    }

    @Test
    void testBackPressureWithLimitedRequests() throws Exception
    {
        List<Integer> list = new ArrayList<>();

        Flux.range(1, 10)
                .log()
                .doOnRequest(r ->
                {
                    LOGGER.debug("Got request of {} on {} thread", r, Thread.currentThread().getName());
                })
                .doOnNext(value ->
                {
                    LOGGER.debug("Got value {} on {} thread", value, Thread.currentThread().getName());
                    list.add(value);
                })
                .subscribe(new CustomBackpressureSubscriber<Integer>(2));

        Integer[] expected = IntStream.range(1, 11).boxed().toArray(Integer[]::new);
        assertEquals(asList(expected), list);
    }

    @Test
    void testBackPressureWithOnePortionOfDataOnly() throws Exception
    {
        List<Integer> list = new ArrayList<>();

        Flux.range(1, 10)
                .doOnNext(value ->
                {
                    LOGGER.debug("Got value {} on {} thread", value, Thread.currentThread().getName());
                    list.add(value);
                })
                .doOnRequest(r ->
                {
                    LOGGER.debug("Got requet {} on {} thread", r, Thread.currentThread().getName());
                })
                .subscribe(new BaseSubscriber<Integer>()
                {
                    @Override
                    public void hookOnSubscribe(Subscription subscription)
                    {
                        request(5);
                    }
                });

        Integer[] expected = IntStream.range(1, 6).boxed().toArray(Integer[]::new);
        assertEquals(asList(expected), list);
    }

    /**
     * Custom implementation of {@link BaseSubscriber} to work with limited requests in the scope of BackPressure.
     * 
     * @param <T> The generic parameter to work in subscriber with.
     */
    private static class CustomBackpressureSubscriber<T> extends BaseSubscriber<T>
    {
        private int limit = 5;
        private int consumed;

        CustomBackpressureSubscriber(int limit)
        {
            this.limit = limit;
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription)
        {
            request(limit);
        }

        @Override
        protected void hookOnNext(T value)
        {
            LOGGER.debug("Perform business logic with {} value", value);

            consumed++;

            if (consumed == limit)
            {
                consumed = 0;
                request(limit);
            }
        }

        @Override
        protected void hookOnComplete()
        {
            LOGGER.debug("super.hookOnComplete()");
        }

        @Override
        protected void hookOnError(Throwable throwable)
        {
            LOGGER.debug("super.hookOnError(throwable)");
        }

        @Override
        protected void hookOnCancel()
        {
            LOGGER.debug("super.hookOnCancel()");
        }

        @Override
        protected void hookFinally(SignalType type)
        {
            LOGGER.debug("super.hookFinally(type)");
        }
    }
}
