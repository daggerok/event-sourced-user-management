package com.github.daggerok.usermanagement.domain;

import java.time.LocalDateTime;
import java.util.Collection;

public interface EventSourcedRepository<T, ID> {
    void save(T aggregate);
    T recreate(ID aggregateId);
    T recreatePast(ID aggregateId, LocalDateTime atTime);
    T replay(T snapshot, Collection<DomainEvent> domainEvents);
    Collection<ID> aggregates();
}
