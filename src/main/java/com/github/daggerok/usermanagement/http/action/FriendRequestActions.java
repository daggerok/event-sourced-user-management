package com.github.daggerok.usermanagement.http.action;

import com.github.daggerok.usermanagement.domain.user.UserInMemoryEventSourcedRepository;
import com.github.daggerok.usermanagement.http.JsonResponse;
import com.github.daggerok.usermanagement.http.Response;
import com.sun.net.httpserver.HttpExchange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FriendRequestActions extends CommonHttpActions {

    @Getter
    private final JsonResponse jsonResponse;
    private final UserInMemoryEventSourcedRepository userRepository;

    /* Public API */

    @SneakyThrows
    public Response send(HttpExchange exchange) {
        log.debug("send friend request...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        return tryWithFallback(exchange, () -> {
            if (System.currentTimeMillis() > 0) throw new RuntimeException("TODO: Not implemented");
            return Response.builder().build().send();
        });
    }

    @SneakyThrows
    public Response accept(HttpExchange exchange) {
        log.debug("accept friend request...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        return tryWithFallback(exchange, () -> {
            throw new RuntimeException("TODO: Not implemented");
        });
    }

    @SneakyThrows
    public Response decline(HttpExchange exchange) {
        log.debug("decline friend request...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        return tryWithFallback(exchange, () -> {
            throw new RuntimeException("TODO: Not implemented");
        });
    }
}
