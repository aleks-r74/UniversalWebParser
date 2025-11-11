package com.alexportfolio.script.host.executor

sealed interface ExecutionResult{
    data class Success(val result: Any?, val logs: String?=null): ExecutionResult
    data class Failure(val error: String, val logs: String?=null): ExecutionResult
    data class Cancelled(val reason: String): ExecutionResult
}
