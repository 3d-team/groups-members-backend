package com.team3d.awad.router;

import com.team3d.awad.router.handler.PresentationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class PresentationRouter {

    @Bean
    public RouterFunction<ServerResponse> presentationRouting(PresentationHandler handler) {
        return route(GET("/api/presentations"), handler::all)
                .andRoute(GET("/api/presentations/{id}"), handler::get)
                .andRoute(POST("/api/presentations"), handler::create)
                .andRoute(PUT("/api/presentations/{id}/slides"), handler::updateSlides)
                .andRoute(POST("/api/presentations/{id}/share"), handler::share)
                .andRoute(POST("/api/presentations/{id}/voting"), handler::voting);
    }
}
