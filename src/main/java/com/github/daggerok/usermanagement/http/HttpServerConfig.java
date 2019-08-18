package com.github.daggerok.usermanagement.http;

import com.github.daggerok.usermanagement.http.action.FriendRequestActions;
import com.github.daggerok.usermanagement.http.action.HttpServerActions;
import com.github.daggerok.usermanagement.http.action.UserAccountActions;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.function.Function;

import static io.vavr.API.*;
import static java.util.function.Predicate.isEqual;

@Log4j2
@Configuration
@RequiredArgsConstructor(onConstructor_ = @Lazy) // <- required!
@ComponentScan(basePackageClasses = HttpServerConfig.class)
public class HttpServerConfig {

    private final HttpServer httpServer;

    @PostConstruct
    public void start() {
        httpServer.start();
    }

    @PreDestroy
    public void stop() {
        httpServer.stop(1);
    }

    @Bean
    public HttpServer httpServer(HttpHandler httpHandler) {
        InetSocketAddress addr = new InetSocketAddress(8080);
        Function<Throwable, RuntimeException> reThrow = RuntimeException::new;
        HttpServer httpServer = Try.of(() -> HttpServer.create(addr, 0))
                                   .getOrElseThrow(reThrow);
        httpServer.createContext("/", httpHandler);
        httpServer.setExecutor(null);
        return httpServer;
    }

    @Bean
    public HttpHandler httpHandler(HttpServerActions httpServerActions,
                                   UserAccountActions userAccountActions,
                                   FriendRequestActions friendRequestActions) {

        return exchange -> Match(exchange.getRequestURI().getPath()).of(
                Case($(isEqual("/user/load")), path -> userAccountActions.load(exchange)),
                Case($(isEqual("/user-account/create")), path -> userAccountActions.create(exchange)),
                Case($(isEqual("/user-account/close")), path -> userAccountActions.close(exchange)),
                Case($(isEqual("/user-account/reactivate")), path -> userAccountActions.reactivate(exchange)),
                Case($(isEqual("/friend-request/send")), path -> friendRequestActions.send(exchange)),
                Case($(isEqual("/friend-request/accept")), path -> friendRequestActions.accept(exchange)),
                Case($(isEqual("/friend-request/decline")), path -> friendRequestActions.decline(exchange)),
                Case($(isEqual("/http-server/shutdown")), path -> httpServerActions.shutdown(exchange)),
                Case($(), path -> httpServerActions.fallbackRestApiInfo(exchange))
        );
    }
}
