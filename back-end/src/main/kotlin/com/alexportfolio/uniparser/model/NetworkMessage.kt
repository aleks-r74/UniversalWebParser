package com.alexportfolio.uniparser.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

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
    companion object { var counter = AtomicLong(0) }
    data class Log(val payload: String, val id:Long = counter.getAndIncrement(), val time: Instant = Instant.now()) : NetworkMessage
    data class Update <T>(val payload: T, val id: Long = counter.getAndIncrement()) : NetworkMessage
}

