package com.alexportfolio.uniparser.controllers

import com.alexportfolio.uniparser.dto.ResultDto
import com.alexportfolio.uniparser.dto.ResultRequestDto
import com.alexportfolio.uniparser.service.ResultService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
@RestController
@RequestMapping("api/results")
class ResultsController(val resultService: ResultService) {
    @GetMapping("{resultId}")
    fun getResult(@PathVariable resultId: Int): ResultDto {
        return resultService.getScriptExecutionResult(resultId)
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("delivered")
    fun markDelivered(@RequestBody updates: List<Int>){
        resultService.markDelivered(updates)
    }

    @PostMapping("group")
    fun getResultsByGroup(@RequestBody request: ResultRequestDto): List<ResultDto> {
        return resultService.findResultsByGroup(request.group, request.delivered)
    }

}