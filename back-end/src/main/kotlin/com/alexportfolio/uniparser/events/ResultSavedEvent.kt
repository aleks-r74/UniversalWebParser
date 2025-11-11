package com.alexportfolio.uniparser.events

import com.alexportfolio.uniparser.dto.ResultDto

data class ResultSavedEvent(val scriptId: Int, val resultId: Int, val content: String)
