package com.alexportfolio.uniparser.dto

data class ResultRowDto(
    val scriptId: Int,
    val resultId: Int,
    val timestamp: String,
    val status: String,
    val delivered: Boolean): UpdatableRow
