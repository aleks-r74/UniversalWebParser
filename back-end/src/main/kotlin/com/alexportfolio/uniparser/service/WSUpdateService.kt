package com.alexportfolio.uniparser.service

import com.alexportfolio.uniparser.dto.ResultRowDto
import com.alexportfolio.uniparser.dto.ScriptRowDto
import com.alexportfolio.uniparser.dto.UpdatableRow
import com.alexportfolio.uniparser.events.DBUpdateEvent
import com.alexportfolio.uniparser.model.NetworkMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass

@Service
class WSUpdateService(private val objectMapper: ObjectMapper,
                      private val ws: WebSocketSessionRegistry) {

    private var msgId = AtomicLong(0)
    private var updateQueue = ConcurrentHashMap<KClass<out UpdatableRow>, ConcurrentHashMap<Int,UpdatableRow>>()

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT,
        fallbackExecution = true // catch event published outside a transaction
    )
    fun onDBUpdateEvent(event: DBUpdateEvent<*>){
        val e = event as? DBUpdateEvent<UpdatableRow> ?: return
        val typeMap = updateQueue.computeIfAbsent(e.payload::class){ ConcurrentHashMap() }
        when(event.payload){
            is ScriptRowDto-> typeMap[event.payload.id] = event.payload
            is ResultRowDto-> typeMap[event.payload.resultId] = event.payload
        }
    }

    @Scheduled(fixedRate=1000)
    fun broadcastUpdates(){
        val events = updateQueue
        updateQueue = ConcurrentHashMap()
        for(typeMap in  events.values){
            typeMap.forEach{ (_,payload)->
                val msg = NetworkMessage.Update(msgId.getAndIncrement(), payload)
                val json = objectMapper.writeValueAsString(msg)
                ws.broadcastAsync(json)
            }
        }
    }

}