package com.github.daggerok.usermanagement.domain;

import java.time.LocalDateTime;
import java.util.Collection;

public interface EventSourcedRepository<T, ID> {
    Collection<ID> aggregates();
    void save(T aggregate);
    T load(ID aggregateId);
    T loadRevision(ID aggregateId, LocalDateTime atTime);
    T replay(T snapshot, Collection<DomainEvent> domainEvents);
}
