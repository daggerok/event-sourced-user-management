package com.github.daggerok.usermanagement.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.daggerok.usermanagement.domain.DomainEvent;
import com.github.daggerok.usermanagement.domain.user.events.*;
import io.vavr.API;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@Getter
@Log4j2
@NoArgsConstructor
@ToString(exclude = "events")
@EqualsAndHashCode(exclude = "events")
public class User implements Function<DomainEvent, User> {

    @JsonIgnore
    private final Collection<DomainEvent> events = new CopyOnWriteArrayList<>();

    /* Aggregate state */

    private UserStatus status = UserStatus.PENDING;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UUID id;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String username;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<UUID> friends = new CopyOnWriteArraySet<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<String> notes = new CopyOnWriteArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<UUID> sentFriendRequests = new CopyOnWriteArraySet<>();

    /* Commands: user account */

    public void createAccount(UUID id, String username) {
        Objects.requireNonNull(id, "id may mot be null.");
        if (id.equals(this.id)) throw new IllegalStateException("user with given identifier is already exists.");

        Objects.requireNonNull(username, "username may mot be null.");
        if ("".equals(username.trim())) throw new IllegalStateException("username may not be empty.");

        apply(UserAccountCreatedEvent.of(id, username));
    }

    public void closeAccount(UUID id, String reason) {
        Objects.requireNonNull(id, "id may not be null.");
        if (status == UserStatus.CLOSED) throw new IllegalStateException("user already suspended.");
        on(UserAccountClosedEvent.of(id, reason));
    }

    public void reactivateAccount(UUID id, String reason) {
        Objects.requireNonNull(id, "id may not be null.");
        if (status == UserStatus.ACTIVE) throw new IllegalStateException("user already active.");
        on(UserAccountReactivatedEvent.of(id, reason));
    }

    /* Commands: friend requests */

    public void sendFriendRequest(UUID toUserId, String greeting) {
        if (status != UserStatus.ACTIVE) throw new IllegalStateException("user is not active.");

        Objects.requireNonNull(toUserId, "to user id may not be null.");
        if (id.equals(toUserId)) throw new IllegalStateException("users cannot send friend requests to themselves.");

        String message = Optional.ofNullable(greeting).orElse(":)");
        on(FriendRequestSentEvent.of(id, toUserId, message));
    }

    public void acceptFriendRequest(UUID fromUserId) {
        if (status != UserStatus.ACTIVE) throw new IllegalStateException("user is not active.");

        Objects.requireNonNull(fromUserId, "from user id may not be null.");
        if (id.equals(fromUserId)) throw new IllegalStateException("users cannot accept requests from themselves.");

        on(FriendRequestAcceptedEvent.of(fromUserId, id));
    }

    public void declineFriendRequest(UUID fromUserId, String reason) {
        if (status != UserStatus.ACTIVE) throw new IllegalStateException("user is not active.");

        Objects.requireNonNull(fromUserId, "from user id may not be null.");
        if (!id.equals(fromUserId)) throw new IllegalStateException("from user id is different to current aggregate.");
        String r = Optional.ofNullable(reason).orElse("");

        on(FriendRequestDeclinedEvent.of(fromUserId, id, r));
    }

    /* Event sourcing */

    // public static User reApply(User snapshot, Collection<DomainEvent> domainEvents) {
    //     return io.vavr.collection.List.ofAll(domainEvents)
    //                                   .foldLeft(snapshot, User::apply);
    // }

    @Override
    public User apply(DomainEvent domainEvent) {
        return API.Match(domainEvent).of(
                Case($(instanceOf(UserAccountCreatedEvent.class)), this::on),
                Case($(instanceOf(UserAccountClosedEvent.class)), this::on),
                Case($(instanceOf(UserAccountReactivatedEvent.class)), this::on),
                Case($(instanceOf(FriendRequestSentEvent.class)), this::on),
                Case($(instanceOf(FriendRequestAcceptedEvent.class)), this::on),
                Case($(instanceOf(FriendRequestDeclinedEvent.class)), this::on),
                Case($(), this::fallbackHandler)
        );
    }

    /* Fallback event handler */

    private <EVENT extends DomainEvent> User fallbackHandler(EVENT unexpected) {
        events.add(unexpected);
        log.error("continue, but make a note, unexpected event was processed: {}", unexpected);
        return this;
    }

    /* Events: user account */

    private User on(UserAccountCreatedEvent event) {
        events.add(event);
        id = event.getId();
        username = event.getUsername();
        status = UserStatus.ACTIVE;
        return this;
    }

    private User on(UserAccountClosedEvent event) {
        events.add(event);
        status = UserStatus.CLOSED;
        notes.add(String.format("%s: %s", event.getAt(), event.getReason()));
        return this;
    }

    private User on(UserAccountReactivatedEvent event) {
        events.add(event);
        status = UserStatus.ACTIVE;
        notes.add(String.format("%s: %s", event.getAt(), event.getReason()));
        return this;
    }

    /* Events: friend requests */

    private User on(FriendRequestSentEvent event) {
        events.add(event);
        sentFriendRequests.add(event.getToUserId());
        return this;
    }

    private User on(FriendRequestDeclinedEvent event) {
        events.add(event);
        sentFriendRequests.removeIf(id -> id.equals(event.getFromUserId()));
        return this;
    }

    private User on(FriendRequestAcceptedEvent event) {
        events.add(event);
        sentFriendRequests.removeIf(id -> id.equals(event.getFromUserId()));
        friends.add(event.getToUserId());
        return this;
    }
}
