package com.github.daggerok.usermanagement.domain;

import java.time.LocalDateTime;
import java.util.Collection;

public interface EventSourcedRepository<T, ID> {
    void save(T aggregate);
    T find(ID aggregateId);
    T findPast(ID aggregateId, LocalDateTime atTime);
    T recreate(T snapshot, Collection<DomainEvent> domainEvents);
}
