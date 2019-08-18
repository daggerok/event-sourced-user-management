package com.github.daggerok.usermanagement.domain.user.events;

import com.github.daggerok.usermanagement.domain.DomainEvent;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value(staticConstructor = "of")
public class FriendRequestDeclinedEvent implements DomainEvent {
    private final UUID fromUserId;
    private final UUID toUserId;
    private final String reason;
    private final LocalDateTime at = LocalDateTime.now();
}
