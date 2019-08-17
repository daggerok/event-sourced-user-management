package com.github.daggerok.usermanagement.domain.user;

import com.github.daggerok.usermanagement.domain.DomainEvent;
import com.github.daggerok.usermanagement.domain.EventSourcedRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vavr.Predicates.not;
import static java.util.Collections.emptyList;

@Log4j2
@Service
public class UserInMemoryEventSourcedRepository implements EventSourcedRepository<User, UUID> {

    private final Map<UUID, Collection<DomainEvent>> eventStore = new ConcurrentHashMap<>();

    @Override
    public void save(User aggregate) {
        User user = Objects.requireNonNull(aggregate, "aggregate may not be null.");
        String type = user.getClass().getSimpleName();
        UUID id = user.getId();
        log.debug("saving {}({}) aggregate.", type, id);

        Collection<DomainEvent> end = new CopyOnWriteArrayList<>(user.getEvents());
        user.getEvents().clear();
        // log.debug("cleared {}({}) aggregate events.", type, id);

        Collection<DomainEvent> begin = eventStore.getOrDefault(id, new CopyOnWriteArrayList<>());
        Collection<DomainEvent> events = Stream.concat(begin.stream(), end.stream())
                                               .collect(Collectors.toList());
        eventStore.put(id, new CopyOnWriteArrayList<>(events));
        // log.debug("aggregate {}({}) saved.", type, id);
    }

    @Override
    public User recreate(UUID aggregateId) {
        log.debug("re-create latest User({}) state from beginning of the history...", aggregateId);

        User snapshot = new User();
        if (Objects.isNull(aggregateId)) return snapshot;

        return eventStore.containsKey(aggregateId)
                ? replay(snapshot, eventStore.get(aggregateId))
                : snapshot;
    }

    @Override
    public User recreatePast(UUID aggregateId, LocalDateTime atTime) {
        log.debug("re-create past User({}) state at {}...", aggregateId, atTime);

        User snapshot = new User();
        if (Objects.isNull(aggregateId)) return snapshot;
        if (eventStore.containsKey(aggregateId)) return snapshot;

        LocalDateTime pastTime = Optional.ofNullable(atTime).orElse(LocalDateTime.now()); // fallback to now

        return replay(snapshot,
                      eventStore.getOrDefault(aggregateId, emptyList()).stream() // stream all events from beginning
                                .filter(not(event -> pastTime.isBefore(event.getAt()))) // filter all past events
                                .collect(Collectors.toList()));
    }

    @Override
    public User replay(User snapshot, Collection<DomainEvent> domainEvents) {
        User user = Optional.ofNullable(snapshot).orElse(new User());
        Collection<DomainEvent> events = Optional.ofNullable(domainEvents).orElse(emptyList());

        log.debug("replay User state from snapshot by applying {} events.", events.size());
        return io.vavr.collection.List.ofAll(events)
                                      .foldLeft(user, User::apply); // Aggregate must have (apply impl) specific API!
    }

    @Override
    public Collection<UUID> aggregates() {
        Set<UUID> identifiers = eventStore.keySet();
        log.debug("aggregate identifiers: {}", identifiers.size());
        return identifiers;
    }
}
