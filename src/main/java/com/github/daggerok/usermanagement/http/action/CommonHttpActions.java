package com.github.daggerok.usermanagement.http.action;

import com.github.daggerok.usermanagement.http.JsonResponse;
import com.github.daggerok.usermanagement.http.Response;
import com.sun.net.httpserver.HttpExchange;
import io.vavr.collection.LinkedHashMap;
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
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public abstract class CommonHttpActions {

    abstract JsonResponse getJsonResponse();

    protected static final Function<HttpExchange, BiFunction<String, String, String>> baseUrl = exchange -> {
        String authority = exchange.getRequestHeaders().getFirst("host");
        return (method, path) ->
                String.format("%s http://%s/%s", Optional.ofNullable(method).orElse(""), authority, path);
    };

    public Response fallbackRestApiInfo(HttpExchange exchange) {
        log.debug("fallback for {}", exchange.getRequestURI());
        BiFunction<String, String, String> url = baseUrl.apply(exchange);
        return getJsonResponse().builder()
                                .body(LinkedHashMap.of("shutdown", url.apply("POST", "server/shutdown"),
                                                       "create user", url.apply("POST", "user/create"),
                                                       "recreate user", url.apply("POST", "user/recreate"))
                                                   .toJavaMap())
                                .httpExchange(exchange)
                                .build()
                                .send();
    }

    protected Response<Object> methodNotSupported(HttpExchange exchange) {
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
}
