package net.soundvibe.reacto.mappers;

import net.soundvibe.reacto.types.*;

/**
 * @author OZY on 2017.01.10.
 */
public interface ServiceRegistryMapper {

    <C> Command toCommand(C genericCommand);

    <E> E toGenericEvent(Event event, Class<? extends E> eventClass);

}
