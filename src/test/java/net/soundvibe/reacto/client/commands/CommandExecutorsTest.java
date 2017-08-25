package net.soundvibe.reacto.client.commands;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author OZY on 2017.01.19.
 */
public class CommandExecutorsTest {

    @Test
    public void shouldCreateReactoFactory() throws Exception {
        final CommandExecutorFactory actual = CommandExecutors.reacto();
        assertNotNull(actual);
    }
}