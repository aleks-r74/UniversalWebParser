package com.alexportfolio.script.definition

interface ScriptStore {
    fun load(): Map<String,String>
    fun save(map: Map<String,String>)
    fun remove(vararg keys: String)
    fun canProcessAfter(url: String, days: Int): Boolean
}