package com.team3d.awad.router;

import com.team3d.awad.router.handler.AuthHandler;
import com.team3d.awad.router.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthRouter {

    @Bean
    public RouterFunction<ServerResponse> authRouting(AuthHandler authHandler, UserHandler userHandler) {
        return route(POST("/api/login"), authHandler::login)
                .andRoute(POST("/api/register"), userHandler::create)
                .andRoute(POST("/api/reset-password"), authHandler::resetPassword)
                .andRoute(POST("/api/change-password"), authHandler::changePassword);
    }
}
