package net.soundvibe.reacto.discovery.vertx;

import io.vertx.core.logging.*;
import io.vertx.servicediscovery.*;
import io.vertx.servicediscovery.Status;
import net.soundvibe.reacto.client.commands.CommandExecutor;
import net.soundvibe.reacto.client.events.EventHandler;
import net.soundvibe.reacto.discovery.*;
import net.soundvibe.reacto.discovery.types.*;
import net.soundvibe.reacto.mappers.ServiceRegistryMapper;
import net.soundvibe.reacto.server.vertx.ServiceRecords;
import net.soundvibe.reacto.types.*;
import net.soundvibe.reacto.types.json.*;
import net.soundvibe.reacto.utils.*;
import rx.Observable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.soundvibe.reacto.discovery.vertx.DiscoverableServices.publishRecord;

/**
 * @author linas on 17.1.9.
 */
public final class VertxServiceRegistry implements ServiceRegistry, ServiceDiscoveryLifecycle<Record>, CommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(VertxServiceRegistry.class);

    private final AtomicBoolean isClosed = new AtomicBoolean(true);

    private final ServiceDiscovery serviceDiscovery;
    private final ServiceRegistryMapper mapper;

    public VertxServiceRegistry(ServiceDiscovery serviceDiscovery, ServiceRegistryMapper mapper) {
        Objects.requireNonNull(serviceDiscovery, "serviceDiscovery cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");
        this.serviceDiscovery = serviceDiscovery;
        this.mapper = mapper;
    }

    @Override
    public Observable<Event> execute(final Command command) {
        return execute(command, LoadBalancers.ROUND_ROBIN);
    }

    private Observable<Event> execute(Command command, LoadBalancer<EventHandler> loadBalancer) {
        return DiscoverableServices.execute(command, serviceDiscovery, loadBalancer);
    }

    @Override
    public <E, C> Observable<? extends E> execute(C command, Class<? extends E> eventClass, LoadBalancer<EventHandler> loadBalancer) {
        if (command == null) return Observable.error(new IllegalArgumentException("command cannot be null"));
        if (eventClass == null) return Observable.error(new IllegalArgumentException("eventClass cannot be null"));
        if (loadBalancer == null) return Observable.error(new IllegalArgumentException("loadBalancer cannot be null"));

        if (command instanceof Command && eventClass.isAssignableFrom(Event.class)) {
            //noinspection unchecked
            return (Observable<E>) execute((Command)command, loadBalancer);
        }

        return Observable.just(command)
                .map(cmd -> mapper.toCommand(cmd, eventClass))
                .concatMap(typedCommand -> execute(typedCommand, loadBalancer)).onBackpressureBuffer()
                .map(event -> mapper.toGenericEvent(event, eventClass));
    }

    @Override
    public Observable<Record> startDiscovery(Record record) {
        log.info("Starting service discovery...");
        return isClosed() ? publish(record)
                .doOnNext(this::startHeartBeat)
                .doOnNext(rec -> Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.info("Executing shutdown hook...");
                    if (isOpen()) {
                        closeDiscovery(rec).subscribe(
                                r -> log.debug("Service discovery closed successfully"),
                                e -> log.debug("Error when closing service discovery: " + e)
                        );
                    }
                })))
                .subscribeOn(Factories.SINGLE_THREAD)
                .doOnCompleted(() -> isClosed.set(false)) :
                Observable.error(new IllegalStateException("Service discovery is already started"));
    }

    @Override
    public Observable<Record> closeDiscovery(Record record) {
        log.info("Closing service discovery...");
        return isOpen() ?
                Observable.just(record)
                        .subscribeOn(Factories.SINGLE_THREAD)
                        .observeOn(Factories.SINGLE_THREAD)
                        .flatMap(rec -> DiscoverableServices.removeIf(rec, ServiceRecords::areEquals, serviceDiscovery))
                        .doOnCompleted(() -> serviceDiscovery.release(serviceDiscovery.getReference(record)))
                        .doOnCompleted(serviceDiscovery::close)
                        .doOnCompleted(() -> isClosed.set(true)) :
                Observable.error(new IllegalStateException("Service discovery is already closed"));
    }

    @Override
    public Observable<Record> publish(Record record) {
        return publishRecord(record, serviceDiscovery);
    }

    @Override
    public boolean isClosed() {
        return isClosed.get();
    }

    @Override
    public boolean isOpen() {
        return !isClosed.get();
    }

    @Override
    public Observable<Record> cleanServices() {
        return DiscoverableServices.removeRecordsWithStatus(Status.DOWN, serviceDiscovery);
    }

    public static ServiceRecord createServiceRecord(Record record) {
        return ServiceRecord.create(
                record.getName(),
                fromStatus(record.getStatus()),
                ServiceType.HTTP_ENDPOINT,
                record.getRegistration(),
                new JsonObject(record.getLocation().getMap()),
                new JsonObject(record.getMetadata().getMap()));
    }

    private static net.soundvibe.reacto.discovery.types.Status fromStatus(Status status) {
        switch (status) {
            case UP:
                return net.soundvibe.reacto.discovery.types.Status.UP;
            case DOWN:
                return net.soundvibe.reacto.discovery.types.Status.DOWN;
            case OUT_OF_SERVICE:
                return net.soundvibe.reacto.discovery.types.Status.OUT_OF_SERVICE;
            case UNKNOWN:
                return net.soundvibe.reacto.discovery.types.Status.UNKNOWN;
            default:
                return net.soundvibe.reacto.discovery.types.Status.UNKNOWN;
        }
    }

    private void startHeartBeat(Record record) {
        Scheduler.scheduleAtFixedInterval(TimeUnit.MINUTES.toMillis(1L), () -> {
            if (isOpen()) {
                publish(record)
                        .subscribe(rec -> log.info("Heartbeat published record: " + rec),
                                throwable -> log.error("Error while trying to publish the record on heartbeat: " + throwable),
                                () -> log.info("Heartbeat completed successfully"));
            } else {
                log.info("Skipping heartbeat because service discovery is closed");
            }
        }, "service-discovery-heartbeat");
    }

}
