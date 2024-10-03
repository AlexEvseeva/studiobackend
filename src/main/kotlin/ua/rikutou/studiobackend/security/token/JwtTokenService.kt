package ua.rikutou.studiobackend.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

class JwtTokenService : TokenService {
    override fun generate(config: TokenConfig, vararg claims: TokenClaim): String =
        JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresIn))
            .apply {
                claims.forEach { claim ->
                    withClaim(claim.name, claim.value)
                }
            }.sign(Algorithm.HMAC256(config.secret))

}