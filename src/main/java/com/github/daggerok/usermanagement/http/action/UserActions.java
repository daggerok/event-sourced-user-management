package com.github.daggerok.usermanagement.http.action;

import com.github.daggerok.usermanagement.domain.user.User;
import com.github.daggerok.usermanagement.domain.user.UserInMemoryEventSourcedRepository;
import com.github.daggerok.usermanagement.http.JsonResponse;
import com.github.daggerok.usermanagement.http.Response;
import com.sun.net.httpserver.HttpExchange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserActions extends CommonHttpActions {

    @Getter
    private final JsonResponse jsonResponse;
    private final UserInMemoryEventSourcedRepository userRepository;

    @SneakyThrows
    public Response createUser(HttpExchange exchange) {
        log.debug("user creation...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        Map<String, String> jsonMap = mapJson(exchange);
        String username = jsonMap.get("username");
        UUID id = Optional.ofNullable(jsonMap.get("id"))
                          .map(UUID::fromString)
                          .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        User user = new User();
        user.create(id, username);
        userRepository.save(user);

        return jsonResponse.builder()
                           .httpExchange(exchange)
                           .body(user)
                           .status(Response.Status.CREATED)
                           .build()
                           .send();
    }

    public Response suspendUser(HttpExchange exchange) {
        log.debug("suspending a user...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        Map<String, String> jsonMap = mapJson(exchange);
        String reason = jsonMap.getOrDefault("reason", "No reasons...");
        UUID id = Optional.ofNullable(jsonMap.get("id"))
                          .map(UUID::fromString)
                          .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        User user = userRepository.recreate(id);
        user.suspend(id, reason);
        userRepository.save(user);

        return jsonResponse.builder()
                           .httpExchange(exchange)
                           .body(user)
                           .status(Response.Status.ACCEPTED)
                           .build()
                           .send();
    }

    public Response reactivateUser(HttpExchange exchange) {
        log.debug("suspending a user...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        Map<String, String> jsonMap = mapJson(exchange);
        String reason = jsonMap.getOrDefault("reason", "With no reason!");
        UUID id = Optional.ofNullable(jsonMap.get("id"))
                          .map(UUID::fromString)
                          .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        User user = userRepository.recreate(id);
        user.reactivate(id, reason);
        userRepository.save(user);

        return jsonResponse.builder()
                           .httpExchange(exchange)
                           .body(user)
                           .status(Response.Status.ACCEPTED)
                           .build()
                           .send();
    }

    @SneakyThrows
    public Response recreateUser(HttpExchange exchange) {
        log.debug("user re-creation...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        Map<String, String> jsonMap = mapJson(exchange);
        UUID id = Optional.ofNullable(jsonMap.get("id"))
                          .map(UUID::fromString)
                          .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        User user = userRepository.recreate(id);

        return jsonResponse.builder()
                           .httpExchange(exchange)
                           .body(user)
                           .status(Response.Status.ACCEPTED)
                           .build()
                           .send();
    }
}
