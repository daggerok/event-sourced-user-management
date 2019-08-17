package com.github.daggerok.usermanagement.domain.user

import com.github.daggerok.usermanagement.domain.EventSourcedRepository
import spock.lang.Specification

class UserEventSourcedRepositoryTest extends Specification {

    EventSourcedRepository repository = new UserInMemoryEventSourcedRepository()

    def "should save user"() {
        given:
            def id = UUID.randomUUID()
        and:
            def user = new User()
        and:
            user.create(id, 'bob')
        and:
            user.suspend(id, 'waiting for approvals')
        and:
            user.events.size() == 2
        and:
            user.notes.size() == 1
        when:
            repository.save(user)
        and:
            user.events.size() == 0
        then:
            def saved = repository.find(id)
        and:
            saved == user
    }
}
