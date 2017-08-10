package net.soundvibe.reacto.types;

import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author OZY on 2017.01.10.
 */
public class MetaDataTest {

    @Test
    public void shouldConcatTwoMetadataInstances() throws Exception {
        final MetaData first = MetaData.of("foo", "bar");
        final MetaData second = first.concat(MetaData.of("key", "value"));

        assertEquals("bar", second.get("foo"));
        assertEquals("value", second.get("key"));
        assertFalse(second.valueOf("unknown").isPresent());
    }

    @Test
    public void shouldBeCreated() throws Exception {
        assertNotNull(MetaData.of("foo", "bar"));
        assertNotNull(MetaData.of("foo", "bar", "foo", "bar"));
        assertNotNull(MetaData.of("foo", "bar", "foo", "bar", "foo", "bar"));
        assertNotNull(MetaData.of("foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar"));
        assertNotNull(MetaData.of("foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar"));
        assertNotNull(MetaData.of("foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar"));
        assertNotNull(MetaData.of("foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar"));
        assertNotNull(MetaData.of("foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar", "foo", "bar",
                "foo", "bar", "foo", "bar"));


        Iterable<Pair<String, String>> iterable = Collections.emptyList();
        assertNotNull(MetaData.fromMap(Collections.emptyMap()));
        assertNotNull(MetaData.from(iterable));

        TestSubscriber<Pair<String,String>> testSubscriber = new TestSubscriber<>();
        MetaData.from(iterable).toObservable().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        assertEquals(0L, MetaData.from(iterable).stream().count());
        assertEquals(0L, MetaData.from(iterable).parallelStream().count());
    }

    @Test
    public void shouldBeEqual() throws Exception {
        final MetaData one = MetaData.of("foo", "bar");
        assertEquals(one, one);
        assertNotEquals(one, null);

        final MetaData two = MetaData.of("foo", "two");
        assertNotEquals(one, two);
    }
}