package com.alexportfolio.uniparser.service

import com.alexportfolio.uniparser.events.ResultSavedEvent
import com.alexportfolio.uniparser.events.ScriptRemovedEvent
import com.alexportfolio.uniparser.events.ScriptSavedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.walk

@Service
class FileService {
    private val basePath = "scripts"
    private val logger = LoggerFactory.getLogger(javaClass)

    fun readScript(id: Int): String{
        val file = Path.of(basePath, id.toString(), "source.kts").toFile()
        require(file.exists()){  "File does not exist: $file" }
        return file.readText()
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun writeScript(event: ScriptSavedEvent){
        val file = Path.of(basePath, event.id.toString(), "source.kts").toFile()
        writeCreating(file, event.source)
        logger.trace("Script ${event.id} saved")
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun deleteScript(event: ScriptRemovedEvent){
        val dir = File("$basePath/${event.id}")
        if(dir.exists()) dir.deleteRecursively()
        logger.trace("Script ${event.id} removed")
    }

    fun readResult(scriptId: Int, resultId: Int): String {
        val file = Path.of(basePath,scriptId.toString(),"results",resultId.toString()).toFile()
        if(!file.exists())
            return "No results available"
        return file.readText()
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun writeResult(event: ResultSavedEvent){
        val resultPath = Path.of(basePath,event.scriptId.toString(),"results",event.resultId.toString())
        val file = resultPath.toFile()
        writeCreating(file, event.content)
        logger.trace("Result for script ${event.scriptId} saved")
    }

    fun writeCompilationLogs(scriptId: Int, logs: String){
        val file = Path.of(basePath, scriptId.toString(), "compilation.log").toFile()
        writeCreating(file, logs)
        logger.trace("Compilation logs for sript  $scriptId saved")
    }

    fun readCompilationLogs(scriptId: Int): String {
        val file = Path.of(basePath, scriptId.toString(), "compilation.log").toFile()
        if(!file.exists())
            return "Compilaiton logs are not available"
        return file.readText()
    }

    fun writeSecret(scriptId: Int, secret: Map<String,String>){
        val path = Path.of(basePath,scriptId.toString(),"secret.properties")
        val properties = Properties()
        properties.putAll(secret)
        FileWriter(path.toFile()).use { fw ->
                properties.store(fw, "")
            }
    }

    fun readSecret(scriptId: Int): ConcurrentHashMap<String, String>{
        val path = Path.of(basePath,scriptId.toString(),"secret.properties")
        val properties =  Properties()
        try{
            FileReader(path.toFile()).use{ fr-> properties.load(fr)}
        } catch (e: IOException){
            logger.error(e.stackTraceToString())
            return ConcurrentHashMap<String,String>()
        }
        return properties.entries.associate { (k, v) -> k.toString() to v.toString() }
            .let { ConcurrentHashMap(it) }
    }

    private fun writeCreating(file: File, content: String) {
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    fun markResultsForRemoval(map: Map<Int,List<Int>>): List<Int>{
        val marked: MutableList<Int> = mutableListOf();
        map.forEach{ entry->
            val scriptId = entry.key.toString()
            entry.value.forEach { resultId->
                try{
                    val path = Path.of(basePath,scriptId,"results",resultId.toString())
                    if(path.exists())
                        Files.move(path,path.resolveSibling(path.fileName.toString() + ".rm"))
                    marked += resultId
                }catch(e: IOException){
                    logger.error("Failed to mark $scriptId/results/$resultId for removal\n", e)
                }
            }
        }
        return marked
    }

    fun removeMarkedResults(){
        Path.of(basePath).walk().forEach { path->
            if(path.fileName.toString().endsWith(".rm"))
                path.deleteIfExists()
        }
    }

    fun saveBroswerState(scriptId:Int, state: String){
        val file = File("${basePath}/$scriptId/state.json")
        if(!file.parentFile.exists()) throw IllegalArgumentException("Script $scriptId doesn't exist")
        file.writeText(state)
    }
}