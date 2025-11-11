package com.alexportfolio.uniparser.model.entity

import com.alexportfolio.uniparser.model.enums.ExecutionStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Table("results")
data class ResultEnt(
    @Id
    val id: Int = 0,
    val scriptId: Int,
    @Column("status")
    val status: ExecutionStatus,
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val resultHash: String? = null,
    val delivered: Int = 0
    )