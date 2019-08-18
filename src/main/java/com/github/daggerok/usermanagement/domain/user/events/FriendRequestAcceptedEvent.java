package com.github.daggerok.usermanagement.domain.user.events;

import com.github.daggerok.usermanagement.domain.DomainEvent;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value(staticConstructor = "of")
public class FriendRequestAcceptedEvent implements DomainEvent {
    private final UUID fromUserId;
    private final UUID toUserId;
    private final LocalDateTime at = LocalDateTime.now();
}
