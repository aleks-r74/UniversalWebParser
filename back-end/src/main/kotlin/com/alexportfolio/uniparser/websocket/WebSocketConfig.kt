package com.alexportfolio.uniparser.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry



@Configuration
@EnableWebSocket
class WebSocketConfig(private val wsHandler: WSHandler,
                      private val handshakeInterceptor: WSHandshakeInterceptor
) : WebSocketConfigurer {
        override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
            registry
                .addHandler(wsHandler,"/queue/private")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns("*")
        }
}
