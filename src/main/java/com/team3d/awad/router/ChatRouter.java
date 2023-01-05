package com.team3d.awad.router;

import com.team3d.awad.router.handler.ChatHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ChatRouter {

    @Bean
    public RouterFunction<ServerResponse> chatRouter(ChatHandler handler) {
        return route(GET("/api/groups/{id}/chats"), handler::all)
                .andRoute(POST("/api/chats"), handler::create);
    }
}
