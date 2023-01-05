package com.team3d.awad.router;

import com.team3d.awad.router.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserRouter {

    @Bean
    public RouterFunction<ServerResponse> userRouting(UserHandler handler) {
        return route(GET("/api/users"), handler::all)
                .andRoute(GET("/api/users/{id}"), handler::get)
                .andRoute(GET("/api/profile"), handler::profile)
                .andRoute(POST("/api/profile"), handler::update)
                .andRoute(POST("/api/users"), handler::create)
                .andRoute(PUT("/api/users/{id}"), handler::update)
                .andRoute(GET("/api/users/activate/{id}"), handler::activate);
    }
}
