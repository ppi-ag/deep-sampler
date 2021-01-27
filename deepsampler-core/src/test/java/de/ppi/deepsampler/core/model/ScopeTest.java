/*
 *
 *  * Copyright 2020 PPI AG (Hamburg, Germany)
 *  * This program is made available under the terms of the MIT License.
 *
 */

package de.ppi.deepsampler.core.model;

import de.ppi.deepsampler.core.api.Sample;
import de.ppi.deepsampler.core.api.Sampler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ScopeTest {

    @Test
    void singletonScopeSharesSamplesAcrossThreads() throws InterruptedException, ExecutionException {
        // WHEN UNCHANGED
        Sampler.clear();
        assertNumberOfSamplers(0);

        //GIVEN
        SampleRepository.setScope(new SingletonScope());

        //WHEN
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<?> thread1 = executorService.submit(() -> {
            TestService sampler = Sampler.prepare(TestService.class);
            Sample.of(sampler.first()).is("The first will be the last");

            assertNumberOfSamplers(1);

            sleep();
            sleep();

            assertNumberOfSamplers(2);
        });

        Future<?> thread2 = executorService.submit(() -> {
            sleep();

            assertNumberOfSamplers(1);

            TestService sampler = Sampler.prepare(TestService.class);
            Sample.of(sampler.second()).is("The second will be the first");
        });

        thread1.get();
        thread2.get();

        // THEN
        assertNumberOfSamplers(2);
    }

    @Test
    void threadScopeDoesNotShareSamplersACrossThreads() throws ExecutionException, InterruptedException {
        // WHEN UNCHANGED
        Sampler.clear();
        assertNumberOfSamplers(0);

        //GIVEN
        SampleRepository.setScope(new ThreadScope());

        //WHEN
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<?> thread1 = executorService.submit(() -> {
            assertNumberOfSamplers(0);

            TestService sampler = Sampler.prepare(TestService.class);
            Sample.of(sampler.first()).is("The first will be the last");

            assertNumberOfSamplers(1);

            sleep();
            sleep();
            sleep();
            sleep();

            assertNumberOfSamplers(1);
        });

        Future<?> thread2 = executorService.submit(() -> {
            assertNumberOfSamplers(0);

            sleep();
            sleep();

            assertNumberOfSamplers(0);

            TestService sampler = Sampler.prepare(TestService.class);
            Sample.of(sampler.second()).is("The first will be the last");

            assertNumberOfSamplers(1);
        });

        thread1.get();
        thread2.get();

        // THEN
        assertNumberOfSamplers(0);
    }

    private void assertNumberOfSamplers(int expectedSamplers) {
        List<SampleDefinition> samples = SampleRepository.getInstance().getSamples();
        assertEquals(expectedSamplers, samples.size());
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    private static class TestService {
        String first() {
            return "First";
        }

        String second() {
            return "Second";
        }
    }
}