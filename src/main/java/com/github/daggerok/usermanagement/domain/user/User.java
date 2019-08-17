package com.github.daggerok.usermanagement.domain.user;

import com.github.daggerok.usermanagement.domain.DomainEvent;
import com.github.daggerok.usermanagement.domain.user.events.UserCreatedEvent;
import com.github.daggerok.usermanagement.domain.user.events.UserReactivatedEvent;
import com.github.daggerok.usermanagement.domain.user.events.UserSuspendEvent;
import io.vavr.API;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@Getter
@Log4j2
@NoArgsConstructor
@EqualsAndHashCode(exclude = "events")
public class User implements Function<DomainEvent, User> {

    private final Collection<DomainEvent> events = new CopyOnWriteArrayList<>();

    private UUID id;
    private String username;
    private UserStatus status = UserStatus.PENDING;
    private Collection<String> notes = new CopyOnWriteArrayList<>();

    /* Commands */

    public void create(UUID id, String username) {
        Objects.requireNonNull(id, "id may mot be null.");
        Objects.requireNonNull(username, "username may mot be null.");
        if ("".equals(username.trim())) throw new IllegalStateException("username may not be empty");
        apply(UserCreatedEvent.of(id, username));
    }

    public void suspend(UUID id, String reason) {
        Objects.requireNonNull(id, "id may not be null");
        if (status == UserStatus.SUSPENDED) throw new IllegalStateException("user already suspended");
        handle(UserSuspendEvent.of(id, reason));
    }

    public void reactivate(UUID id, String reason) {
        Objects.requireNonNull(id, "id may not be null");
        if (status == UserStatus.ACTIVE) throw new IllegalStateException("user already active");
        handle(UserReactivatedEvent.of(id, reason));
    }

    /* Event sourcing */

    // public static User reApply(User snapshot, Collection<DomainEvent> domainEvents) {
    //     return io.vavr.collection.List.ofAll(domainEvents)
    //                                   .foldLeft(snapshot, User::apply);
    // }

    @Override
    public User apply(DomainEvent domainEvent) {
        return API.Match(domainEvent).of(
                Case($(instanceOf(UserCreatedEvent.class)), this::handle),
                Case($(instanceOf(UserSuspendEvent.class)), this::handle),
                Case($(instanceOf(UserReactivatedEvent.class)), this::handle),
                Case($(), this::handle)
        );
    }

    /* Events */

    private User handle(UserCreatedEvent event) {
        events.add(event);
        id = event.getId();
        username = event.getUsername();
        status = UserStatus.ACTIVE;
        return this;
    }

    private User handle(UserSuspendEvent event) {
        events.add(event);
        this.status = UserStatus.SUSPENDED;
        notes.add(String.format("%s: %s", event.getAt(), event.getReason()));
        return this;
    }

    private User handle(UserReactivatedEvent event) {
        events.add(event);
        this.status = UserStatus.ACTIVE;
        notes.add(String.format("%s: %s", event.getAt(), event.getReason()));
        return this;
    }

    /* Fallback */

    private <T extends DomainEvent> User handle(T unexpected) {
        events.add(unexpected);
        log.error("continue, but make a note, unexpected event was processed: {}", unexpected);
        return this;
    }
}
