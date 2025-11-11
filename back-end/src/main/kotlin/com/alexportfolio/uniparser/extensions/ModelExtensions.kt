package com.alexportfolio.uniparser.extensions

import com.alexportfolio.script.host.executor.ExecutionResult
import com.alexportfolio.uniparser.dto.*
import com.alexportfolio.uniparser.model.entity.ResultEnt
import com.alexportfolio.uniparser.model.entity.ScriptEnt
import com.alexportfolio.uniparser.model.enums.ExecutionStatus

fun CreateScriptRequest.toEntity(): ScriptEnt{
    return ScriptEnt(name = name, group=group, hash = source.sha256())
}
fun ScriptEnt.toScriptContent(source: String): ScriptContentDto{
    return ScriptContentDto(id, source,name, group)
}
fun ScriptEnt.toScriptRow(): ScriptRowDto{
    return ScriptRowDto(id,name,group,compileFailed==0,runCount,failCount,nextRun,enabled>0,enqueued>0)
}
fun ResultEnt.toResultRowDto(): ResultRowDto {
    return ResultRowDto(scriptId,id,timestamp, status.name, delivered>0)
}

fun ResultEnt.toResultDto(name: String, result: String): ResultDto{
    return ResultDto(scriptId, id, name, timestamp, resultHash, status, result, delivered>0)
}

fun ExecutionResult.toStatus(): ExecutionStatus = when(this){
        is ExecutionResult.Success ->   ExecutionStatus.SUCCESS
        is ExecutionResult.Failure ->   ExecutionStatus.FAILED
        is ExecutionResult.Cancelled -> ExecutionStatus.CANCELLED
    }