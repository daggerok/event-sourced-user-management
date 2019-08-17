package com.github.daggerok.usermanagement.domain.user

import spock.lang.Specification

import java.util.stream.Stream

class UserTest extends Specification {

    def 'should create user'() {
        given:
            def user = new User()
        expect:
            user.status == UserStatus.PENDING
        and:
            user.id == null
        when:
            user.create(UUID.fromString('00000000-0000-0000-0000-000000000000'), 'bob')
        then:
            user.status == UserStatus.ACTIVE
        expect:
            user.username == 'bob'
        and:
            user.events.size() == 1
        and:
            user.id as String == '00000000-0000-0000-0000-000000000000'
    }

    def 'should suspend user'() {
        given:
            def user = new User()
        and:
            def id = UUID.fromString('00000000-0000-0000-0000-000000000000')
        and:
            user.create(id, 'bob')
        when:
            user.suspend(id, 'seems like bob was hacked')
        then:
            user.status == UserStatus.SUSPENDED
        and:
            user.notes.size() == 1
    }

    def 'should re-activate user'() {
        given:
            def user = new User()
        and:
            def id = UUID.fromString('00000000-0000-0000-0000-000000000000')
        and:
            user.create(id, 'bob')
        and:
            user.events.size() == 1
        and:
            user.notes.size() == 1
        and:
            user.suspend(id, 'freedom speech, motherfuckers!')
        and:
            user.events.size() == 2
        and:
            user.notes.size() == 1
        when:
            user.reactivate(id, 'we are sorry for this misunderstanding...')
        and:
            user.events.size() == 3
        and:
            user.notes.size() == 2
        then:
            user.status == UserStatus.ACTIVE
        and:
            user.notes.first().endsWith 'freedom speech, motherfuckers!'
        and:
            user.notes.last().endsWith 'we are sorry for this misunderstanding...'
    }
}
