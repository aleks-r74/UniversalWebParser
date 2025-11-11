package com.alexportfolio.uniparser.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDateTime

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = NetworkMessage.Log::class, name = "LOG"),
    JsonSubTypes.Type(value = NetworkMessage.Update::class, name = "UPDATE")
)
sealed interface NetworkMessage{
    data class Log(val payload: String, val time: LocalDateTime = LocalDateTime.now()) : NetworkMessage
    data class Update <T>(val id: Long, val payload: T) : NetworkMessage
}

