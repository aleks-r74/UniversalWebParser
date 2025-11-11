package com.alexportfolio.uniparser.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class DuplicateScriptSourceException(message: String): RuntimeException(message) {
}