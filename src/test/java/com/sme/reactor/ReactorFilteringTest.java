package com.sme.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;

/**
 * Filters data in pipeline.
 */
public class ReactorFilteringTest
{
    @Test
    public void testFilter()
    {
        List<Integer> list = new ArrayList<>();

        Flux.just(IntStream.range(1, 10).boxed().toArray(Integer[]::new))
                .filter(value -> value < 6)
                .subscribe(list::add);

        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void testDistinct()
    {
        List<Integer> list = new ArrayList<>();

        Flux.just(7, 5, 1, 2, 3, 4, 4, 5, 5, 8, 6, 7, 7)
                .distinct()
                .subscribe(e -> list.add(e));

        assertEquals(Arrays.asList(7, 5, 1, 2, 3, 4, 8, 6), list);
    }

    @Test
    public void testLast()
    {
        List<Integer> list = new ArrayList<>();

        Flux.just(IntStream.range(1, 10).boxed().toArray(Integer[]::new))
                .last()
                .subscribe(list::add);

        assertEquals(Arrays.asList(9), list);
    }

    @Test
    public void testSkip()
    {
        List<Integer> list = new ArrayList<>();

        Flux.just(IntStream.range(1, 10).boxed().toArray(Integer[]::new))
                .skip(7)
                .subscribe(list::add);

        assertEquals(Arrays.asList(8, 9), list);
    }

    @Test
    public void testTake()
    {
        List<Integer> list = new ArrayList<>();

        Flux.just(IntStream.range(1, 10).boxed().toArray(Integer[]::new))
                .take(3)
                .subscribe(list::add);

        assertEquals(Arrays.asList(1, 2, 3), list);
    }
}
