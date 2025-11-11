package com.alexportfolio.uniparser.dto

data class ScriptRowDto(val id: Int,
                        val name: String,
                        val group: String,
                        val compiles: Boolean,
                        val runs: Int,
                        val fails: Int,
                        val nextRun: String,
                        val isEnabled: Boolean,
                        val isEnqueued: Boolean
): UpdatableRow