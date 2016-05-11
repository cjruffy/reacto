package net.soundvibe.reacto.types;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Cipolinas on 2016.02.18.
 */
public class CommandTest {

    @Test
    public void shouldBeAbleToFindInSet() throws Exception {
        final Command testCmd = Command.create("test");
        final Command metaCmd = Command.create("test", MetaData.of("one", "two"));
        final Command payloadCmd = Command.create("test", Optional.empty(), Optional.of("data".getBytes()));

        Set<Command> commands = new HashSet<>();
        commands.add(testCmd);
        commands.add(metaCmd);
        commands.add(payloadCmd);

        assertTrue("Command with name 'test' not found", commands.contains(testCmd));
        assertTrue("Command with name 'test' and metadata not found", commands.contains(metaCmd));
        assertTrue("Command with payload not found", commands.contains(payloadCmd));
        assertFalse("Should not find because ID's should be different", commands.contains(Command.create("test")));
    }

}