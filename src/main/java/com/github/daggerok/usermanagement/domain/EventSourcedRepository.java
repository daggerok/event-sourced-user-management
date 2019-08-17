package com.github.daggerok.usermanagement.domain;

public interface EventSourcedRepository<T, ID> {
    void save(T aggregate);
    T find(ID aggregateId);
}
