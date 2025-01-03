package ua.rikutou.studiobackend.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject
import ua.rikutou.studiobackend.security.token.TokenConfig

fun Application.configureSecurity() {
    val config by inject<TokenConfig>()
    authentication {
        jwt {
            realm = this@configureSecurity.environment.config.property("jwt.realm").getString()
            verifier(
                JWT
                    .require(Algorithm.HMAC256(config.secret))
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .build()
            )
            validate { jwtCredential ->
                if(jwtCredential.payload.audience.contains(config.audience)) {
                    JWTPrincipal(jwtCredential.payload)
                } else null
            }
        }
    }
}
