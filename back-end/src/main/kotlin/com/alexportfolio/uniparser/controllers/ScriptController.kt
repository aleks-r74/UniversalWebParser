package com.alexportfolio.uniparser.controllers

import com.alexportfolio.uniparser.dto.*
import com.alexportfolio.uniparser.extensions.toScriptRow
import com.alexportfolio.uniparser.model.enums.ScriptAction
import com.alexportfolio.uniparser.service.FileService
import com.alexportfolio.uniparser.service.ResultService
import com.alexportfolio.uniparser.service.ScriptService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
@RestController
@RequestMapping("api/scripts")
class ScriptController(val scriptService: ScriptService,
                       val resultService: ResultService,
                       val fs: FileService){
    @GetMapping
    fun getAll(): List<ScriptRowDto>{
        return scriptService.findAll().map{  it.toScriptRow() }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun createScript(@RequestBody script: CreateScriptRequest): CreateScriptResponse {
        return CreateScriptResponse(scriptService.createScript(script))
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    fun updateScript(@PathVariable id: Int, @RequestBody script: CreateScriptRequest){
        scriptService.updateScript(id,script)
    }

    @GetMapping("/{id}")
    fun getScript(@PathVariable id: Int): ScriptContentDto {
        return scriptService.getScriptContent(id)
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteScript(@PathVariable id: Int){
        scriptService.deleteScript(id)
    }

    @GetMapping("/{id}/results")
    fun getResults(@PathVariable id: Int): List<ResultRowDto> {
        return resultService.getResults(id)
    }
    @GetMapping("/{id}/clogs")
    fun getCompilationLog(@PathVariable id: Int): String {
        return fs.readCompilationLogs(id)
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/actions")
    fun doAction(@PathVariable id: Int, @RequestBody action: ActionDto){
        when(action.action){
            ScriptAction.ENABLE -> scriptService.setEnabled(id, true)
            ScriptAction.DISABLE -> scriptService.setEnabled(id, false)
            ScriptAction.FORCE_STOP -> scriptService.forceStop(id)
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/secret")
    fun saveSecret(@PathVariable id: Int, @RequestBody secret: Map<String,String>){
        scriptService.saveSecret(id,secret)
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/secret")
    fun getSecret(@PathVariable id: Int): Map<String,String>{
        return scriptService.getSecret(id)
    }
}