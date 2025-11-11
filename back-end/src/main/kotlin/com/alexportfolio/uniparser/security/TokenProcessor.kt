package com.alexportfolio.uniparser.security
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKey

@Component
class TokenProcessor(@Value("\${app.jwt.secret}") private val secretBase64: String) {

    private val TOKEN_LIFE_SEC = 86_400L

    private val privateKey: SecretKey =  if(!secretBase64.isBlank()){
            Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64))
        }else{
            val keyBytes = ByteArray(32)
            SecureRandom().nextBytes(keyBytes)
            Keys.hmacShaKeyFor(keyBytes)
        }

    fun generateToken(auth: Authentication): String {
        val now = Date()
        val expiresAt = Date(now.toInstant().plusSeconds(TOKEN_LIFE_SEC).toEpochMilli())
        val roles = auth.authorities.map { it.authority }

        return Jwts.builder()
            .subject(auth.name)
            .claim("roles", roles)
            .signWith(privateKey)
            .issuedAt(now)
            .expiration(expiresAt)
            .compact()
    }

    fun getAuthObjFromToken(token: String?): Authentication? {
        if (token.isNullOrBlank()) return null

        return try {
            val payload = Jwts.parser()
                .verifyWith(privateKey)
                .build()
                .parseSignedClaims(token)
                .payload

            val username = payload.subject ?: return null

            val rawRoles = payload["roles"]
            val rolesList: List<String> = when (rawRoles) {
                is List<*> -> rawRoles.filterNotNull().map { it.toString() }
                is Array<*> -> rawRoles.filterNotNull().map { it.toString() }
                is String -> rawRoles.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                else -> emptyList()
            }

            val authorities = AuthorityUtils.createAuthorityList(*rolesList.toTypedArray())
            UsernamePasswordAuthenticationToken(username, null, authorities)
        } catch (e: JwtException) {
            null
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
}
