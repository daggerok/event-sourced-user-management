package com.github.daggerok.usermanagement.domain.user;

import com.github.daggerok.usermanagement.domain.DomainEvent;
import com.github.daggerok.usermanagement.domain.EventSourcedRepository;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class UserInMemoryEventSourcedRepository implements EventSourcedRepository<User, UUID> {

    private final Map<UUID, Collection<DomainEvent>> eventStore = new ConcurrentHashMap<>();

    @Override
    public void save(User aggregate) {
        String type = aggregate.getClass().getSimpleName();
        UUID id = aggregate.getId();
        log.debug("about to save {}({}) aggregate...", type, id);

        Collection<DomainEvent> events = new CopyOnWriteArrayList<>(aggregate.getEvents());
        aggregate.getEvents().clear();
        log.debug("cleared {}({}) aggregate events.", type, id);

        Collection<DomainEvent> curr = new CopyOnWriteArrayList<>(
                eventStore.getOrDefault(id, new CopyOnWriteArrayList<>()));
        eventStore.put(id, new CopyOnWriteArrayList<>(Stream.concat(curr.stream(), events.stream())
                                                            .collect(Collectors.toList())));
        log.debug("aggregate {}({}) saved.", type, id);
    }

    @Override
    public User find(UUID aggregateId) {
        log.debug("start rebuild process for aggregate: {}", aggregateId);
        User snapshot = new User();
        return eventStore.containsKey(aggregateId)
                ? User.restore(snapshot, eventStore.get(aggregateId))
                : snapshot;
    }
}
