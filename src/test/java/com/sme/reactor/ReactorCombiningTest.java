package com.sme.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

/**
 * Unit tests of different Flux operations.
 */
public class ReactorCombiningTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactorCombiningTest.class);

    /**
     * Test Flux#zip in the same thread.
     */
    @Test
    public void testZip()
    {
        Flux<String> flux1 = Flux.just("hello ");
        Flux<String> flux2 = Flux.just("reactive ");
        Flux<String> flux3 = Flux.just("world");
        Flux<String> flux4 = Flux.just("!");

        StringBuilder result = new StringBuilder("");

        Flux.zip(flux1, flux2, flux3, flux4)
                .map(tuple4 ->
                {
                    LOGGER.debug("Map {} tuple", tuple4);
                    return tuple4.getT1().concat(tuple4.getT2()).concat(tuple4.getT3().concat(tuple4.getT4()));
                })
                .map(String::toUpperCase)
                .subscribe(value ->
                {
                    LOGGER.debug("Prepard {} result", value);
                    result.append(value);
                });

        assertEquals("HELLO REACTIVE WORLD!", result.toString());
    }

    @Test
    public void testMerge()
    {
        Flux<String> flux1 = Flux.just("Hello ")
                .doOnNext(value ->
                {
                    LOGGER.debug("Sleep {} ms in flux1", 2000);
                    sleep(2000);
                });

        Flux<String> flux2 = Flux.just("reactive ")
                .doOnNext(value ->
                {
                    LOGGER.debug("Sleep {} ms in flux2", 5000);
                    sleep(5000);
                });

        Flux<String> flux3 = Flux.just("world!");

        StringBuilder result = new StringBuilder("");

        Flux.merge(flux1, flux2, flux3)
                .map(String::toUpperCase)
                .subscribe(value ->
                {
                    LOGGER.debug("Prepard {} result", value);
                    result.append(value);
                });

        assertEquals("HELLO REACTIVE WORLD!", result.toString());
    }

    @Test
    public void testConcat()
    {
        Flux<String> flux1 = Flux.just("Hello ")
                .doOnNext(value ->
                {
                    LOGGER.debug("Sleep {} ms in flux1", 2000);
                    sleep(2000);
                });

        Flux<String> flux2 = Flux.just("reactive ")
                .doOnNext(value ->
                {
                    LOGGER.debug("Sleep {} ms in flux2", 5000);
                    sleep(5000);
                });

        Flux<String> flux3 = Flux.just("world!")
                .doOnNext(value ->
                {
                    LOGGER.debug("Sleep {} ms in flux3", 3000);
                    sleep(3000);
                });

        StringBuilder result = new StringBuilder("");
        Flux.concat(flux1, flux2, flux3)
                .map(String::toUpperCase)
                .subscribe(value ->
                {
                    LOGGER.debug("Prepard {} result", value);
                    result.append(value);
                });

        assertEquals("HELLO REACTIVE WORLD!", result.toString());
    }

    @Test
    public void testSwitchMap()
    {
        StringBuilder result = new StringBuilder("");

        Flux.just(12, 3, 40, 4, 50, 1)
                .switchMap(value ->
                {
                    return value >= 10 ? Flux.just(value / 10) : Flux.just(value);
                })
                .map(v -> v + ",")
                .subscribe(value ->
                {
                    LOGGER.debug("Prepard {} result", value);
                    result.append(value);
                });

        assertEquals("1,3,4,4,5,1,", result.toString());
    }

    private static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
