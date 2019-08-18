package com.github.daggerok.usermanagement.domain.user

import com.github.daggerok.usermanagement.domain.EventSourcedRepository
import spock.lang.Specification

class SendFriendRequestTest extends Specification {

    EventSourcedRepository repository = new UserInMemoryEventSourcedRepository()

    def 'bad, split it into few tests cases: users should be able to send friend request to other users, but not to themselves'() {
        given:
            def fromUser = new User()
            fromUser.createAccount(UUID.randomUUID(), "user")
            repository.save(fromUser)
        and:
            def toUser = new User()
            toUser.createAccount(UUID.randomUUID(), "friend")
            repository.save(toUser)
        when:
            fromUser.sendFriendRequest(fromUser.id, "Hola!")
        then:
            def e = thrown(IllegalStateException)
            e.localizedMessage == 'users cannot send friend requests to themselves.'
        and:
            fromUser.sendFriendRequest(toUser.id, "Hola!")
            repository.save(fromUser)
        and:
            def loaded = repository.load(fromUser.getId())
            loaded.sentFriendRequests.size() == 1
    }

    def 'better, test case 1: users should be able to send friend request to other users'() {
        given:
            def fromUser = new User()
            fromUser.createAccount(UUID.randomUUID(), "user")
            repository.save(fromUser)
        and:
            def toUser = new User()
            toUser.createAccount(UUID.randomUUID(), "friend")
            repository.save(toUser)
        when:
            fromUser.sendFriendRequest(toUser.id, "Hola!")
            repository.save(fromUser)
        then:
            def loaded = repository.load(fromUser.getId())
            loaded.sentFriendRequests.size() == 1
    }

    def 'better, test case 2: users should not be able to send friend request to themselves'() {
        given:
            def fromUser = new User()
            fromUser.createAccount(UUID.randomUUID(), "user")
            repository.save(fromUser)
        and:
            def toUser = new User()
            toUser.createAccount(UUID.randomUUID(), "friend")
            repository.save(toUser)
        when:
            fromUser.sendFriendRequest(fromUser.id, "Hola!")
        then:
            def e = thrown(IllegalStateException)
            e.localizedMessage == 'users cannot send friend requests to themselves.'
    }
}
