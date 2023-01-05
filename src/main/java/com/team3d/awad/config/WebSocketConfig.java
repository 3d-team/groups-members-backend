package com.team3d.awad.config;

import com.team3d.awad.ws.PresentationHostHandler;
import com.team3d.awad.ws.PresentationJoinerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConfig {

    private final PresentationJoinerHandler joinerHandler;
    private final PresentationHostHandler hostHandler;

    public WebSocketConfig(PresentationJoinerHandler joinerHandler,
                           PresentationHostHandler hostHandler) {
        this.joinerHandler = joinerHandler;
        this.hostHandler = hostHandler;
    }

    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> handlers = mappingHandlers();
        return configureHandlers(handlers);
    }

    private Map<String, WebSocketHandler> mappingHandlers() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/presentation/host", hostHandler);
        map.put("/ws/presentation/join", joinerHandler);
        return map;
    }

    private SimpleUrlHandlerMapping configureHandlers(Map<String, WebSocketHandler> handlers) {
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setUrlMap(handlers);
        return handlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
//
//    @Bean
//    public WebSocketService webSocketService() {
//        return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy());
//    }
}
