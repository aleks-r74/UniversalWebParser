package com.alexportfolio.uniparser.service

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.io.IOException

import java.util.concurrent.ConcurrentHashMap


@Service
class WebSocketSessionRegistry {
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    private val sendSemaphore = Semaphore(10)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val sessionLocks = ConcurrentHashMap<String, Mutex>()

    fun register(session: WebSocketSession) {
        sessions[session.id] = session
    }

    fun unregister(session: WebSocketSession) {
        sessions.remove(session.id, session)
        sessionLocks.remove(session.id)
    }

    suspend fun broadcast(msg: String){
        withContext(Dispatchers.IO){
            supervisorScope {
                sessions.values.map{ session->
                    async{
                        sendSemaphore.withPermit { sendMessage(session, msg) }
                    }
                }.awaitAll()
            }
        }
    }

    fun broadcastAsync(msg: String): Job = scope.launch { broadcast(msg) }

    private suspend fun sendMessage(session: WebSocketSession, msg: String){
        val lock = sessionLocks.computeIfAbsent(session.id){ Mutex() }
        lock.withLock {
            try {
                if (session.isOpen) {
                    session.sendMessage(TextMessage(msg))
                } else {
                    sessions.remove(session.id)
                }
            } catch (e: IOException) {
                sessions.remove(session.id)
                try { session.takeIf { it.isOpen }?.close() } catch (_: Exception) {}
            }
        }

    }

    @PreDestroy
    fun cancel() = scope.cancel()
}