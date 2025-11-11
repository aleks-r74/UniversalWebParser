package com.alexportfolio.uniparser.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource
import java.util.*

@Configuration
@EnableMethodSecurity
class SecurityConfig @Autowired constructor(private val corsConfigurationSource: CorsConfigurationSource? = null,
                                            @Value("\${users.admin.password}") private val admPass: String,
                                            @Value("\${users.guest.password}") private val gstPass: String) {
    @Bean
    fun userManager(encoder: PasswordEncoder): UserDetailsManager {
        val user1 = User
            .withUsername("admin")
            .password(encoder.encode(admPass))
            .roles("ADMIN")
            .build()
        val user2 = User
            .withUsername("guest")
            .password(
                encoder.encode(
                    if(gstPass.isBlank()) UUID.randomUUID().toString() else gstPass
                )
            )
            .roles("USER")
            .build()
        return InMemoryUserDetailsManager(user1,user2)
    }

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtAuthFilter: JWTAuthFilter): SecurityFilterChain {
        http
            .cors {cors->
                corsConfigurationSource?.let{cors.configurationSource(it)}
            }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/", "/index.html", "/favicon.ico",
                        "/*.js", "/*.css").permitAll()
                    .requestMatchers("/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}