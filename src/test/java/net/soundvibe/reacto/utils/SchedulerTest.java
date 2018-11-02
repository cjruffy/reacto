package net.soundvibe.reacto.utils;

import org.junit.Test;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

/**
 * @author OZY on 2017.01.23.
 */
public class SchedulerTest {

    @Test
    public void shouldSetTimer() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        final Timer actual = Scheduler.scheduleAtFixedInterval(10L, counter::incrementAndGet, "test");
        Thread.sleep(52L);
        actual.cancel();

        assertTrue(counter.get() > 0);
    }

    @Test
    public void shouldCallEvenWhenRunnableThrows() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        final Timer actual = Scheduler.scheduleAtFixedInterval(100L, () -> {
            if (counter.incrementAndGet() == 1) {
                throw new IllegalStateException("error");
            }

        }, "test");
        Thread.sleep(225L);
        actual.cancel();
        assertTrue(counter.get() > 1);
    }
}