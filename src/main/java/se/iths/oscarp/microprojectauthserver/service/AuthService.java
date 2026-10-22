package se.iths.oscarp.microprojectauthserver.service;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import se.iths.oscarp.microprojectauthserver.dto.AuthResult;
import se.iths.oscarp.microprojectauthserver.dto.LoginRequestDTO;
import se.iths.oscarp.microprojectauthserver.dto.TokenResponseDTO;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

// Handles user authentication, JWT generation, and public key distribution.
@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final KeyPair keyPair;
    private final String jwtIssuer;
    private final long jwtExpirationMinutes;
    private final String jwtKeyId;

    // Injects the authentication manager, JWT encoder, key pair, and token settings.
    public AuthService(AuthenticationManager authenticationManager,
                       JwtEncoder jwtEncoder,
                       KeyPair keyPair,
                       @Value("${app.jwt.issuer}") String jwtIssuer,
                       @Value("${app.jwt.expiration-minutes}") long jwtExpirationMinutes,
                       @Value("${app.jwt.key-id}") String jwtKeyId) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.keyPair = keyPair;
        this.jwtIssuer = jwtIssuer;
        this.jwtExpirationMinutes = jwtExpirationMinutes;
        this.jwtKeyId = jwtKeyId;
    }

    // Validates the user's credentials and generates an access token.
    public AuthResult login(LoginRequestDTO request) {

        // Authenticates the user with the supplied username and password.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        UserDetails principal = (UserDetails) authentication.getPrincipal();

        // Extracts the user's assigned roles from their granted authorities.
        List<String> roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .toList();

        // Sets the token's issue time and expiration time.
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtExpirationMinutes, ChronoUnit.MINUTES);

        // Defines the claims included in the JWT.
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(principal.getUsername())
                .claim("roles", roles);

        // Configures the token to use RS256 signing and the specified key ID.
        JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(jwtKeyId)
                .build();

        // Signs the claims and generates the serialized JWT access token.
        String accessToken = jwtEncoder.encode(
                JwtEncoderParameters.from(jwsHeader, claimsBuilder.build())
        ).getTokenValue();

        // Returns the access token and its associated response details.
        return new AuthResult(
                accessToken,
                new TokenResponseDTO(
                        jwtExpirationMinutes * 60L,
                        principal.getUsername(),
                        roles
                )
        );
    }

    // Publishes the RSA public key as a JWK set for JWT verification.
    public Map<String, Object> publicJwkSet() {
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .keyID(jwtKeyId)
                .build();
        return new JWKSet(rsaKey).toJSONObject();
    }
}
