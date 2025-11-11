package com.alexportfolio.uniparser.model

import com.alexportfolio.uniparser.model.entity.ResultEnt

data class ResultWithContent(
    val record: ResultEnt,
    val content: String=""
)
