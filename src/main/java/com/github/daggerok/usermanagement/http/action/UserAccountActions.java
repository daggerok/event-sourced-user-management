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
import java.util.function.Consumer;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserAccountActions extends CommonHttpActions {

    @Getter
    private final JsonResponse jsonResponse;
    private final UserInMemoryEventSourcedRepository userRepository;

    /* Public API */

    @SneakyThrows
    public Response create(HttpExchange exchange) {
        log.debug("user creation...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        return tryWithFallback(exchange, () -> {

            Map<String, String> jsonMap = mapJson(exchange);
            String username = jsonMap.get("username");
            UUID id = id(jsonMap);

            User created = loadFinallyAccept(id, user -> user.createAccount(id, username));

            return jsonResponse.builder()
                               .body(created)
                               .status(Response.Status.CREATED)
                               .httpExchange(exchange)
                               .build()
                               .send();
        });
    }

    public Response close(HttpExchange exchange) {
        log.debug("suspending a user...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        return tryWithFallback(exchange, () -> {

            Map<String, String> jsonMap = mapJson(exchange);
            String reason = jsonMap.getOrDefault("reason", "Closed with no reasons...");
            UUID id = id(jsonMap);

            User accepted = loadFinallyAccept(id, user -> user.closeAccount(id, reason));

            return jsonResponse.builder()
                               .body(accepted)
                               .status(Response.Status.ACCEPTED)
                               .httpExchange(exchange)
                               .build()
                               .send();
        });
    }

    public Response reactivate(HttpExchange exchange) {
        log.debug("suspending a user...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        return tryWithFallback(exchange, () -> {

            Map<String, String> jsonMap = mapJson(exchange);
            String reason = jsonMap.getOrDefault("reason", "Reactivated with no reason!");
            UUID id = id(jsonMap);

            User accepted = loadFinallyAccept(id, user -> user.reactivateAccount(id, reason));

            return jsonResponse.builder()
                               .body(accepted)
                               .status(Response.Status.ACCEPTED)
                               .httpExchange(exchange)
                               .build()
                               .send();
        });
    }

    @SneakyThrows
    public Response load(HttpExchange exchange) {
        log.debug("user re-creation...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        return tryWithFallback(exchange, () -> {

            Map<String, String> jsonMap = mapJson(exchange);
            UUID id = id(jsonMap);

            return jsonResponse.builder()
                               .httpExchange(exchange)
                               .body(userRepository.load(id))
                               .status(Response.Status.ACCEPTED)
                               .build()
                               .send();
        });
    }

    /* Private API */

    private UUID id(Map<String, String> jsonMap) {
        return Optional.ofNullable(jsonMap.get("id"))
                       .map(UUID::fromString)
                       .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

    private User loadFinallyAccept(UUID id, Consumer<User> processor) {
        User user = userRepository.load(id);
        processor.accept(user);
        userRepository.save(user);
        return user;
    }
}
