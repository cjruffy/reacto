package reactive.fp.types;

import rx.Observable;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Linas on 2015.11.13.
 */

public final class Event implements Message {

    public final Optional<MetaData> metaData;
    public final Optional<byte[]> payload;
    public final EventType eventType;
    public final Optional<ReactiveException> error;

    Event(Optional<MetaData> metaData, Optional<byte[]> payload, Optional<Throwable> error, EventType eventType) {
        this.metaData = metaData;
        this.payload = payload;
        this.eventType = eventType;
        this.error = error.map(ReactiveException::from);
    }

    Event(Throwable error) {
        this.metaData = Optional.empty();
        this.payload = Optional.empty();
        this.eventType = EventType.ERROR;
        this.error = Optional.ofNullable(error).map(ReactiveException::from);
    }

    public String get(String key) {
        return metaData.map(pairs -> pairs.get(key)).orElse(null);
    }

    public Optional<String> valueOf(String key) {
        return metaData.flatMap(pairs -> pairs.valueOf(key));
    }

    public Observable<Event> toObservable() {
        return Observable.just(this);
    }

    public static Event create(Pair... metaDataPairs) {
        return onNext(Optional.of(MetaData.from(metaDataPairs)), Optional.empty());
    }

    public static Event create(MetaData metaData) {
        return onNext(Optional.ofNullable(metaData), Optional.empty());
    }

    public static Event create(byte[] payload) {
        return onNext(Optional.empty(), Optional.ofNullable(payload));
    }

    public static Event create(MetaData metaData, byte[] payload) {
        return onNext(Optional.ofNullable(metaData), Optional.ofNullable(payload));
    }

    public static Event create(Optional<MetaData> metaData, Optional<byte[]> payload) {
        return onNext(metaData, payload);
    }

    static Event onNext(Optional<MetaData> metaData, Optional<byte[]> payload) {
        return new Event(metaData, payload, Optional.empty(),EventType.NEXT);
    }

    public static Event onError(Throwable throwable) {
        return new Event(throwable);
    }

     public static Event onCompleted() {
        return new Event(Optional.empty(), Optional.empty(), Optional.empty(), EventType.COMPLETED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(metaData, event.metaData) &&
                Objects.equals(payload, event.payload) &&
                eventType == event.eventType &&
                Objects.equals(error, event.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metaData, payload, eventType, error);
    }

    @Override
    public String toString() {
        return "Event{" +
                "metaData=" + metaData +
                ", payload=" + payload +
                ", eventType=" + eventType +
                ", error=" + error +
                '}';
    }
}
