package com.github.daggerok.usermanagement.http.action;

import com.github.daggerok.usermanagement.http.JsonResponse;
import com.github.daggerok.usermanagement.http.Response;
import com.sun.net.httpserver.HttpExchange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
@RequiredArgsConstructor
public class ServerActions extends CommonHttpActions {

    @Getter
    private final JsonResponse jsonResponse;

    public Response shutdownServer(HttpExchange exchange) {
        log.debug("stop server endpoint...");

        if (isNotPostMethod(exchange)) return methodNotSupported(exchange);

        return jsonResponse.builder()
                           .body("Exiting..... Bye!")
                           .status(Response.Status.ACCEPTED)
                           .httpExchange(exchange)
                           .postProcessor(() -> CompletableFuture
                                   .runAsync(() -> exchange.getHttpContext()
                                                           .getServer()
                                                           .stop(5000))
                                   .thenAccept(aVoid -> {
                                       log.info("completion...");
                                       if (System.currentTimeMillis() % 4 == 0)
                                           throw new RuntimeException("canary!");
                                   })
                                   .exceptionally(throwable -> {
                                       log.error("oops: {}", throwable.getLocalizedMessage());
                                       return null;
                                   }))
                           .build()
                           .send();
    }
}
