package com.alexportfolio.script.definition

interface ScriptStore {
    fun load(): MutableMap<String,String>
    fun save(map: Map<String,String>)
    fun isThrottled(url: String, days: Int): Boolean
    val memoryMap: MutableMap<String,Any>
}