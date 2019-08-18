package com.github.daggerok.usermanagement.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.vavr.control.Try;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonMap;

@Value
@Log4j2
@Builder
@Getter(AccessLevel.PRIVATE)
public class Response<T> {

    private final T body;
    private final Response.Status status;
    private final HttpExchange httpExchange;
    private final ObjectMapper objectMapper;
    private final Supplier postProcessor;

    private String mapToJson(Object input) {
        return Try.of(() -> objectMapper.writeValueAsString(input))
                  .getOrElseGet(throwable -> String.format("{\"error\":\"%s\"}", throwable.getLocalizedMessage()));
    }

    @SneakyThrows
    public Response<T> send() {
        log.info("message: {}", body);
        @Cleanup val exchange = httpExchange;
        setJsonContent(exchange);
        setCors(exchange);
        sendResponse(exchange);
        setupPostProcessor();
        return this;
    }

    private void setupPostProcessor() {
        Optional.ofNullable(postProcessor)
                .ifPresent(Supplier::get);
    }

    private void sendResponse(HttpExchange exchange) throws IOException {
        // T payload = Objects.requireNonNull(body, "message is required!");
        T payload = Optional.ofNullable(body).orElse((T) null);
        String jsonResponse = mapToJson(singletonMap("result", payload));
        exchange.sendResponseHeaders(status.code, jsonResponse.length());
        @Cleanup val out = exchange.getResponseBody();
        out.write(jsonResponse.getBytes(UTF_8));
    }

    private void setCors(HttpExchange exchange) {
        String allowHeaders = "Content-Type";
        String allowMethods = "GET, PUT, POST, DELETE, OPTIONS";
        String allowOrigin = Optional.ofNullable(exchange.getRequestHeaders()
                                                         .getFirst("Origin"))
                                     .orElse("null");
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Allow", allowMethods);
        responseHeaders.set("Access-Control-Allow-Origin", allowOrigin);
        responseHeaders.set("Access-Control-Allow-Methods", allowMethods);
        responseHeaders.set("Access-Control-Allow-Headers", allowHeaders);
        responseHeaders.set("Access-Control-Request-Methods", allowMethods);
        responseHeaders.set("Access-Control-Request-Headers", allowHeaders);
        responseHeaders.set("Access-Control-Max-Age", "3600");
    }

    private void setJsonContent(HttpExchange exchange) {
        exchange.getResponseHeaders()
                .set("Content-Type", String.format("application/json; charset=%s", UTF_8));
    }

    @RequiredArgsConstructor
    public enum Status {
        OK(200),
        CREATED(201),
        ACCEPTED(202),
        BAD_REQUEST(400);
        public final int code;
    }
}
