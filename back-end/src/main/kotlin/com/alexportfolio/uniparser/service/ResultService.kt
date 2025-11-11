package com.alexportfolio.uniparser.service

import com.alexportfolio.uniparser.dto.ResultDto
import com.alexportfolio.uniparser.dto.ResultRowDto
import com.alexportfolio.uniparser.events.DBUpdateEvent
import com.alexportfolio.uniparser.events.ResultSavedEvent
import com.alexportfolio.uniparser.extensions.toResultDto
import com.alexportfolio.uniparser.extensions.toResultRowDto

import com.alexportfolio.uniparser.model.ResultWithContent
import com.alexportfolio.uniparser.model.entity.ResultEnt
import com.alexportfolio.uniparser.model.enums.ExecutionStatus
import com.alexportfolio.uniparser.repositories.ResultRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ResultService(private val resultRepository: ResultRepository,
                    private val scriptService: ScriptService,
                    private val fs: FileService,
                    private val jdbcTemplate: JdbcTemplate,
                    private val eventPublisher: ApplicationEventPublisher,
                    @Value("\${scripts.results.limit}") private val resultLimit: Int
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getResults(scriptId: Int): List<ResultRowDto>{
        val result = resultRepository.findAllByScriptId(scriptId)
        return result.map { it.toResultRowDto() }.sortedByDescending { it.timestamp }
    }

    fun getScriptExecutionResult(resultId: Int): ResultDto {
        val record = resultRepository.findById(resultId).orElseThrow{IllegalArgumentException("$resultId not found")}
        val name = scriptService.getNameById(record.scriptId)
        val result = fs.readResult(record.scriptId, resultId)
        return record.toResultDto(name,result)
    }

    fun markDelivered(ids: List<Int>) {
        val sql = "UPDATE results SET delivered = 1 WHERE id = ?"
        val batchParams = ids.map { arrayOf(it) }
        jdbcTemplate.batchUpdate(sql, batchParams)
    }

    fun findResultsByGroup(group: String, delivered: Boolean?): List<ResultDto>{
        return resultRepository.findResultsByGroup(group)
            .filter{ resultEnt-> delivered?.let { it == resultEnt.delivered>0 } ?: true}
            .map{resultEnt->
                val name = scriptService.getNameById(resultEnt.scriptId)
                val result = fs.readResult(resultEnt.scriptId, resultEnt.id)
                resultEnt.toResultDto(name,result)
            }
    }

    @Transactional
    fun saveResult(resultWContent: ResultWithContent): ResultEnt {
        var (result, content)  = resultWContent
        when (result.status) {
            ExecutionStatus.SUCCESS -> scriptService.resetFailCounter(result.scriptId)
            ExecutionStatus.FAILED -> scriptService.updateFailCounter(result.scriptId)
            else -> {}
        }
        var oldRecord = resultRepository.findByScriptIdAndResultHash(result.scriptId,result.resultHash)
        if(oldRecord!=null){
            logger.info("Result for script ${result.scriptId} is not unique, only timestamp updated")
            result = oldRecord.copy(timestamp = LocalDateTime.now().toString())
        }
        val saved = resultRepository.save(result)
        if(oldRecord == null)
            eventPublisher.publishEvent(ResultSavedEvent(saved.scriptId, saved.id, content))
        eventPublisher.publishEvent(DBUpdateEvent(saved.toResultRowDto()))
        return saved
    }

    fun findOldResults(): Map<Int,List<Int>>{
        return resultRepository.findScriptsWithResults().map{ scriptId->
            scriptId to resultRepository.findTailResultsForScript(scriptId, resultLimit)
        }.toMap()
    }

    @Transactional
    fun deleteBatchByIds(list: List<Int>) = resultRepository.deleteBatchByIdList(list)

}
