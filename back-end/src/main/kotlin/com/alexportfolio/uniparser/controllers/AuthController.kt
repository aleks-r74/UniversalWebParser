package com.alexportfolio.uniparser.controllers

import com.alexportfolio.uniparser.security.TokenProcessor
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
class AuthController(private val tokenProcessor: TokenProcessor,
                     private val authenticationManager: AuthenticationManager,) {

    data class LoginRequest(val username: String, val password: String)
    data class TokenDto(val token: String)

    @PostMapping("/auth/login")
    fun login(@RequestBody loginRequest: LoginRequest): TokenDto {
        val authToken = UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
        val auth: Authentication = authenticationManager.authenticate(authToken)
        return TokenDto(tokenProcessor.generateToken(auth))
    }

}