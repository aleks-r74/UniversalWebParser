package com.alexportfolio.script.definition

interface ScriptStore {
    fun load(): Map<String,String>

}