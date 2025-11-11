package com.alexportfolio.uniparser.dto

import com.alexportfolio.uniparser.model.enums.ExecutionStatus
import java.time.LocalDateTime
/* contains information about individual result along with the script output in the result*/
data class ResultDto(
    val scriptId: Int,
    val resultId: Int,
    val scriptName: String,
    val timestamp: String,
    val resultHash: String? = null,
    val status: ExecutionStatus,
    val result: String,
    val delivered: Boolean
)
