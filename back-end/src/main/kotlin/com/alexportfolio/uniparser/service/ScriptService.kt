package com.alexportfolio.uniparser.service

import com.alexportfolio.script.definition.ScriptStore
import com.alexportfolio.uniparser.dto.CreateScriptRequest
import com.alexportfolio.uniparser.dto.ScriptContentDto
import com.alexportfolio.uniparser.events.DBUpdateEvent
import com.alexportfolio.uniparser.events.ForceStopEvent
import com.alexportfolio.uniparser.events.ScriptRemovedEvent
import com.alexportfolio.uniparser.events.ScriptSavedEvent
import com.alexportfolio.uniparser.exceptions.DuplicateScriptNameException
import com.alexportfolio.uniparser.exceptions.DuplicateScriptSourceException
import com.alexportfolio.uniparser.exceptions.ScriptNotFoundException
import com.alexportfolio.uniparser.extensions.sha256
import com.alexportfolio.uniparser.extensions.toEntity
import com.alexportfolio.uniparser.extensions.toScriptContent
import com.alexportfolio.uniparser.extensions.toScriptRow
import com.alexportfolio.uniparser.model.entity.ScriptEnt
import com.alexportfolio.uniparser.repositories.ScriptRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ConcurrentHashMap


@Service
class ScriptService(private val scriptRepository: ScriptRepository,
                    private val fs: FileService,
                    private val eventPublisher: ApplicationEventPublisher,
                    private val scriptRegistry: RunningScriptsRegistry
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val storeCache = ConcurrentHashMap<Int,ScriptStore>()

    fun getScriptContent(scriptId: Int): ScriptContentDto {
        val ent = findById(scriptId)
        val source = fs.readScript(scriptId)
        return ent.toScriptContent(source)
    }

    @Transactional
    fun createScript(script: CreateScriptRequest): Int {
        checkDuplicateScript(script.source.sha256(), script.name)
        val record = scriptRepository.save(script.toEntity())
        eventPublisher.publishEvent(ScriptSavedEvent(record.id,script.source))
        return record.id
    }

    @Transactional
    fun updateScript(scriptId: Int, script: CreateScriptRequest){
        var record = findById(scriptId)
        checkDuplicateScript(script.source.sha256(), script.name, scriptId)
        record = ScriptEnt(record.id,name=script.name,script.group,script.source.sha256())
        val saved = scriptRepository.save(record)
        eventPublisher.publishEvent(DBUpdateEvent(saved.toScriptRow()))
        eventPublisher.publishEvent(ScriptSavedEvent(saved.id,script.source))
    }

    @Transactional
    fun deleteScript(scriptId: Int){
        scriptRegistry.cancelJob(scriptId)
        scriptRepository.deleteById(scriptId)
        eventPublisher.publishEvent(ScriptRemovedEvent(scriptId))
    }
    fun saveSecret(scriptId: Int, secret: Map<String, String>){
        fs.writeSecret(scriptId,secret)
    }
    fun getSecret(scriptId: Int): Map<String,String>{
        return fs.readSecret(scriptId)
    }
    private fun checkDuplicateScript(hash: String, name: String, ignoreId: Int = 0) {
        val ent = scriptRepository.findByHashOrName(hash, name)
        if (ent.isPresent && ent.get().id != ignoreId) {
            if (ent.get().name == name) {
                throw DuplicateScriptNameException("Script name '$name' already exists (id=${ent.get().id})")
            } else {
                throw DuplicateScriptSourceException("Script source with hash '$hash' already exists (id=${ent.get().id})")
            }
        }
    }

    fun findById(id: Int): ScriptEnt = scriptRepository.findById(id).orElseThrow {throw ScriptNotFoundException("$id not found")}

    fun getNameById(id: Int): String{
        val rec = scriptRepository.findById(id).orElseThrow{ throw ScriptNotFoundException("${id} not found") }
        return rec.name
    }

    fun findAll(): MutableIterable<ScriptEnt> = scriptRepository.findAll()

    fun markEnqueued(scriptId: Int) {
        scriptRepository.markEnqueued(scriptId)?.let{
            eventPublisher.publishEvent(DBUpdateEvent(it.toScriptRow()))
        }
    }

    fun resetEnqueued(scriptId: Int) {
        scriptRepository.resetEnqueued(scriptId)?.let{
            eventPublisher.publishEvent(DBUpdateEvent(it.toScriptRow()))
        }
    }

    fun resetFailCounter(scriptId: Int) = scriptRepository.resetFailCounter(scriptId)

    fun failEnqueued() = scriptRepository.failEnqueued()

    @Transactional
    fun updateFailCounter(scriptId: Int) {
        if(scriptRepository.updateFailCounter(scriptId)==3)
            scriptRepository.disable(scriptId)
    }

    fun setEnabled(scriptId: Int, enabled: Boolean) {
        logger.info(if(enabled)"Script $scriptId enabled" else "Script $scriptId disabled")
        val record = if (enabled) scriptRepository.enable(scriptId) else scriptRepository.disable(scriptId)
        record?.let{ eventPublisher.publishEvent(DBUpdateEvent(it.toScriptRow()))}
    }

    @Transactional
    fun setCompilationSuccess(scriptId: Int, success: Boolean){
        val record = if(success) {
            scriptRepository.enable(scriptId)
            scriptRepository.setCompileSucceed(scriptId)
        }else {
            scriptRepository.setCompileFailed(scriptId)
            scriptRepository.disable(scriptId)
        }
        record?.let{ eventPublisher.publishEvent(DBUpdateEvent(it.toScriptRow()))}
    }

    fun forceStop(scriptId: Int){
        setEnabled(scriptId,false)
        eventPublisher.publishEvent(ForceStopEvent(scriptId))
    }

   fun getStore(scriptId: Int) = storeCache.computeIfAbsent(scriptId){ createStore(scriptId) }

   private fun createStore(scriptId: Int) = object : ScriptStore {
            val fileCache: MutableMap<Int,Map<String,String>> = mutableMapOf()
            val backoffMap = ConcurrentHashMap<String,Long>()

            override fun load() = fileCache.getOrPut(scriptId){ fs.readSecret(scriptId) }
            override fun save(map: Map<String, String>){
                fs.writeSecret(scriptId, map)
                fileCache[scriptId]=map
            }
            override fun remove(vararg keys: String) {
                fileCache[scriptId] = load()
                    .filterKeys { it !in keys }
                    .also{ save(it) }
            }
            override fun canProcessAfter(url: String, hours: Int): Boolean {
                val daysToMillis = hours * 60 * 60 * 1000L
                val hash = url.sha256()
                val now = System.currentTimeMillis()
                backoffMap.entries.removeIf { (_, timestamp) -> now - timestamp >= daysToMillis }
                val allow = backoffMap.putIfAbsent(hash, now) == null
                return allow
            }
        }
}