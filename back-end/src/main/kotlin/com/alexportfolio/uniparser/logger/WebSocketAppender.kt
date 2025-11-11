package com.alexportfolio.uniparser.logger

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import com.alexportfolio.uniparser.model.NetworkMessage
import com.alexportfolio.uniparser.service.WebSocketSessionRegistry
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WebSocketAppender(private val ws: WebSocketSessionRegistry,
                        private val objectMapper: ObjectMapper
): AppenderBase<ILoggingEvent>() {

    override fun append(eventObject: ILoggingEvent) {
       val msg = eventObject.formattedMessage
       ws.broadcastAsync(
           objectMapper.writeValueAsString(NetworkMessage.Log(msg))
       )
    }

    @PostConstruct
    fun init() {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        this.context = context
        start()
        val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        logger.addAppender(this)
    }
}