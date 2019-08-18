package com.github.daggerok.usermanagement.domain.user

import com.github.daggerok.usermanagement.domain.EventSourcedRepository
import spock.lang.Specification

class UserEventSourcedRepositoryTest extends Specification {

    EventSourcedRepository repository = new UserInMemoryEventSourcedRepository()

    def 'should save, create, suspend user and recreate it from beginning of the history by its identifier'() {
        given:
            def id = UUID.randomUUID()
        and:
            def user = new User()
        and:
            user.createAccount(id, 'bob')
        and:
            user.closeAccount(id, 'waiting for approvals')
        and:
            user.events.size() == 2
        and:
            user.notes.size() == 1
        when:
            repository.save(user)
        and:
            user.events.size() == 0
        then:
            def loaded = repository.load(id)
        and:
            loaded == user
    }

    def 'should create two users and verify all aggregates are present in repository after their save'() {
        given:
            def user1 = new User()
        and:
            user1.createAccount(UUID.randomUUID(), UUID.randomUUID() as String)
        and:
            def user2 = new User()
        and:
            user2.createAccount(UUID.randomUUID(), UUID.randomUUID() as String)
        expect:
            repository.aggregates().size() == 0
        when:
            repository.save(user1)
        and:
            repository.save(user2)
        then:
            repository.aggregates().size() == 2

    }
}
