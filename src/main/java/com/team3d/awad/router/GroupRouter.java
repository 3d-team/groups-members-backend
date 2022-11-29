package com.team3d.awad.router;

import com.team3d.awad.repository.GroupRepository;
import com.team3d.awad.router.handler.GroupHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class GroupRouter {

    @Bean
    public RouterFunction<ServerResponse> routes(GroupHandler handler) {
        return route(GET("/api/groups"), handler::all)
                .andRoute(POST("/api/groups"), handler::create)
                .andRoute(GET("/api/groups/{id}"), handler::get)
                .andRoute(PUT("/api/groups/{id}"), handler::update)
                .andRoute(DELETE("/api/groups/{id}"), handler::delete)
                .andRoute(POST("/api/groups/{id}/members"), handler::addMember)
                .andRoute(DELETE("/api/groups/{id}/members/{memberId}"), handler::removeMember)
                .andRoute(POST("/api/groups/{id}/co-owners"), handler::addCoOwner)
                .andRoute(DELETE("/api/groups/{id}/co-owners/{coOwnerId}"), handler::removeCoOwner);
    }
}
