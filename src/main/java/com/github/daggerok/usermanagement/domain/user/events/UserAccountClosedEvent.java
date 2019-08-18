package com.github.daggerok.usermanagement.domain.user.events;

import com.github.daggerok.usermanagement.domain.DomainEvent;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value(staticConstructor = "of")
public class UserAccountClosedEvent implements DomainEvent {
    private final UUID id;
    private final String reason;
    private final LocalDateTime at = LocalDateTime.now();
}
