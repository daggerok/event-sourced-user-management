package com.github.daggerok.usermanagement.http.action;

import com.github.daggerok.usermanagement.http.JsonResponse;
import com.github.daggerok.usermanagement.http.Response;
import com.sun.net.httpserver.HttpExchange;
import io.vavr.collection.LinkedHashMap;
import io.vavr.control.Try;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public abstract class CommonHttpActions {

    abstract JsonResponse getJsonResponse();

    protected static final Function<HttpExchange, BiFunction<String, String, String>> baseUrl = exchange ->
            (method, path) -> String.format("%s http://%s/%s",
                                            Optional.ofNullable(method).orElse(""),
                                            exchange.getRequestHeaders().getFirst("host"), path);

    private static final Function<BiFunction<String, String, String>, Map<String, String>> restApi = url ->
            LinkedHashMap.of("shutdown server", url.apply("POST", "http-server/shutdown"),
                             "create user account", url.apply("POST", "user-account/create"),
                             "reactivate user account", url.apply("POST", "user-account/reactivate"),
                             "close user account", url.apply("POST", "user-account/close"),
                             "recreate user", url.apply("POST", "user/load"))
                         .toJavaMap();

    public Response fallbackRestApiInfo(HttpExchange exchange) {
        log.debug("fallback for {}", exchange.getRequestURI());

        return tryWithFallback(exchange, () -> {
            BiFunction<String, String, String> url = baseUrl.apply(exchange);
            return getJsonResponse().builder()
                                    .body(restApi.apply(url))
                                    .httpExchange(exchange)
                                    .build()
                                    .send();
        });
    }

    protected Response methodNotSupported(HttpExchange exchange) {
        return getJsonResponse().builder()
                                .httpExchange(exchange)
                                .body("only post method is supported")
                                .status(Response.Status.BAD_REQUEST)
                                .build()
                                .send();
    }

    protected boolean isNotPostMethod(HttpExchange exchange) {
        return !"post".equalsIgnoreCase(exchange.getRequestMethod());
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    protected Map<String, String> mapJson(HttpExchange exchange) {
        @Cleanup InputStream inputStream = exchange.getRequestBody();
        @Cleanup InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        @Cleanup BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String json = bufferedReader.lines()
                                    .map(String::trim)
                                    .collect(Collectors.joining(""));
        return (Map<String, String>) getJsonResponse().getObjectMapper().readValue(json, Map.class);
    }

    protected Response tryWithFallback(HttpExchange exchange, Supplier<Response> mayFail) {
        return Try.of(mayFail::get)
                  .getOrElseGet(throwable -> getJsonResponse().builder()
                                                              .body(String.format("%s: %s",
                                                                                  throwable.getClass().getSimpleName(),
                                                                                  throwable.getLocalizedMessage()))
                                                              .httpExchange(exchange)
                                                              .status(Response.Status.BAD_REQUEST)
                                                              .build()
                                                              .send());
    }
}
