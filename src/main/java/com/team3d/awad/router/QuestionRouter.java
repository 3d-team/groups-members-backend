package com.team3d.awad.router;

import com.team3d.awad.router.handler.QuestionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class QuestionRouter {

    @Bean
    public RouterFunction<ServerResponse> questionRouter(QuestionHandler handler) {
        return route(GET("/api/groups/{id}/questions"), handler::all)
                .andRoute(GET("/api/questions/{id}"), handler::get)
                .andRoute(POST("/api/questions"), handler::create)
                .andRoute(POST("/api/questions/{id}"), handler::handle)
                .andRoute(GET("/api/questions/{id}/answers"), handler::allAnswers);
    }
}
