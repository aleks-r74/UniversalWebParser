package com.alexportfolio.uniparser.websocket

import com.alexportfolio.uniparser.service.WebSocketSessionRegistry
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler


@Component
class WSHandler(private val registry: WebSocketSessionRegistry) : TextWebSocketHandler() {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        registry.register(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val auth = session.attributes["auth"] as? Authentication
        logger.info("Received: $message from ${auth?.name}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        registry.unregister(session)
    }
}