package com.alexportfolio.uniparser.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("scripts")
data class ScriptEnt(
    @Id
    val id: Int = 0,
    val name: String,
    @Column("group_name")
    val group: String,
    val hash: String,
    val nextRun: String = Instant.now().toString(),
    val enabled: Int = 1,
    val compileFailed: Int = 0,
    val enqueued: Int = 0,
    val runCount: Int = 0,
    val failCount: Int = 0
)

