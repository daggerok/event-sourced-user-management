package com.github.daggerok.usermanagement.domain;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime getAt();
}
