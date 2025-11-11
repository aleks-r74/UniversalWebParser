package com.alexportfolio.uniparser.repositories

import com.alexportfolio.uniparser.model.entity.ScriptEnt
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface ScriptRepository: CrudRepository<ScriptEnt, Int> {
    fun findByHashOrName(hash: String, name: String): Optional<ScriptEnt>
    @Modifying
    @Query("""
        UPDATE scripts
        SET enqueued = 0, fail_count = fail_count + 1
        WHERE enqueued = 1
    """)
    fun failEnqueued()

    @Query("""
        UPDATE scripts
        SET enqueued = 1, run_count = run_count + 1
        WHERE id = :id AND enqueued = 0
        RETURNING *
    """)
    fun markEnqueued(@Param("id") id: Int): ScriptEnt?

    @Query("""
        UPDATE scripts
        SET enqueued = 0,
            next_run = strftime('%Y-%m-%dT%H:%M:%fZ', 'now', '+5 minutes')
        WHERE id = :id AND enqueued = 1
        RETURNING *
    """)
    fun resetEnqueued(@Param("id") id: Int): ScriptEnt?

    @Query("""
        UPDATE scripts
        SET fail_count = fail_count + 1
        WHERE id = :id
        RETURNING fail_count
    """)
    fun updateFailCounter(@Param("id") id: Int): Int

    @Modifying
    @Query("""
        UPDATE scripts
        SET fail_count = 0
        WHERE id = :id
    """)
    fun resetFailCounter(@Param("id") id: Int)

    @Query("""
        UPDATE scripts
        SET enabled = 1, fail_count = 0
        WHERE id = :id AND enabled = 0
        RETURNING *
    """)
    fun enable(@Param("id") id: Int): ScriptEnt?

    @Query("""
        UPDATE scripts
        SET enabled = 0
        WHERE id = :id AND enabled = 1
        RETURNING *
    """)
    fun disable(@Param("id") id: Int): ScriptEnt?

    @Query("""
        UPDATE scripts
        SET compile_failed = 1
        WHERE id = :id AND compile_failed = 0
        RETURNING *
    """)
    fun setCompileFailed(@Param("id") id: Int): ScriptEnt?

    @Query("""
        UPDATE scripts
        SET compile_failed = 0
        WHERE id = :id AND compile_failed = 1
        RETURNING *
    """)
    fun setCompileSucceed(@Param("id") id: Int): ScriptEnt?
}