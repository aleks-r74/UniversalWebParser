package com.alexportfolio.uniparser.repositories

import com.alexportfolio.uniparser.model.entity.ResultEnt
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param


interface ResultRepository : PagingAndSortingRepository<ResultEnt,Int>, CrudRepository<ResultEnt, Int> {

    fun findAllByScriptId(id: Int): List<ResultEnt>

    fun findByScriptIdAndResultHash(scriptId: Int, hash: String?): ResultEnt?
    @Query("""
        SELECT r.*
        FROM results r
        JOIN scripts s ON r.script_id = s.id
        WHERE s.group_name = :group
    """)
    fun findResultsByGroup(@Param("group") group: String): List<ResultEnt>

    @Query("""
        SELECT id FROM results
        WHERE script_id = :id
        ORDER BY timestamp DESC
        LIMIT -1 OFFSET :n
    """)
    fun findTailResultsForScript(@Param("id") id: Int, @Param("n") n: Int): List<Int>

    @Query("""
        SELECT DISTINCT script_id FROM results 
    """)
    fun findScriptsWithResults(): List<Int>

    @Modifying
    @Query("DELETE FROM results WHERE id IN (:list)\n")
    fun deleteBatchByIdList(list: List<Int>): Int
}