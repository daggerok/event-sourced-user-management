package com.github.daggerok.usermanagement.domain.user;

import com.github.daggerok.usermanagement.domain.DomainEvent;
import com.github.daggerok.usermanagement.domain.EventSourcedRepository;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vavr.Predicates.not;
import static java.util.Collections.emptyList;

@Log4j2
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
    public User find(UUID aggregateId) {
        return findPast(aggregateId, LocalDateTime.now());
    }

    @Override
    public User findPast(UUID aggregateId, LocalDateTime atTime) {
        UUID id = Objects.requireNonNull(aggregateId, "aggregate ID may not be null.");
        LocalDateTime pastTime = Optional.ofNullable(atTime).orElse(LocalDateTime.now()); // fallback to now
        return recreate(new User(),
                        eventStore.getOrDefault(id, emptyList())
                                  .stream()
                                  .filter(not(event -> pastTime.isBefore(event.getAt()))) // all past events
                                  .collect(Collectors.toList()));
    }

    @Override
    public User recreate(User snapshot, Collection<DomainEvent> domainEvents) {
        User user = Optional.ofNullable(snapshot).orElse(new User());
        Collection<DomainEvent> events = Optional.ofNullable(domainEvents).orElse(emptyList());

        log.debug("re-creation {} state by applying {} events.",
                  user.getClass().getSimpleName(), events.size());
        return io.vavr.collection.List.ofAll(events)
                                      .foldLeft(user, User::apply);
    }
}
