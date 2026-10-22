package se.iths.oscarp.microprojectauthserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import se.iths.oscarp.microprojectauthserver.security.CookieBearerTokenResolver;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

// Configures application security, JWT handling, password encoding, and CORS.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String jwtIssuer;
    private final String jwtPublicKey;
    private final String jwtPrivateKey;
    private final String jwtKeyId;

    // Injects JWT settings from the application configuration.
    public SecurityConfig(@Value("${app.jwt.issuer}") String jwtIssuer,
                          @Value("${app.jwt.public-key:}") String jwtPublicKey,
                          @Value("${app.jwt.private-key:}") String jwtPrivateKey,
                          @Value("${app.jwt.key-id}") String jwtKeyId
    ) {
        this.jwtIssuer = jwtIssuer;
        this.jwtPublicKey = jwtPublicKey;
        this.jwtPrivateKey = jwtPrivateKey;
        this.jwtKeyId = jwtKeyId;
    }

    // Defines access rules and configures JWT authentication for incoming requests.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disables CSRF protection and enables the configured CORS rules.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                })

                .authorizeHttpRequests(authorize -> authorize
                        // Allows unauthenticated access to login, documentation, and public endpoints.
                        .requestMatchers(
                                "/auth/login",
                                "/auth/logout",
                                "/auth/jwks",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/appusers",
                                "/error"
                        ).permitAll()
                        // Requires authentication for user-specific endpoints.
                        .requestMatchers("/appusers/**").authenticated()
                        // Requires authentication for all other endpoints.
                        .anyRequest().authenticated()
                )
                // Extracts bearer tokens using the custom resolver and validates them as JWTs.
                .oauth2ResourceServer(oauth2 ->
                        oauth2
                                .bearerTokenResolver(new CookieBearerTokenResolver())
                                .jwt(Customizer.withDefaults())
                );

        return http.build();
    }

    // Builds an RSA key pair from the configured Base64-encoded keys.
    @Bean
    public KeyPair keyPair() throws Exception {
        if (StringUtils.hasText(jwtPrivateKey) && StringUtils.hasText(jwtPublicKey)) {
            byte[] privateBytes = Base64.getDecoder().decode(jwtPrivateKey);
            byte[] publicBytes = Base64.getDecoder().decode(jwtPublicKey);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            // Decodes the private and public keys using their respective formats.
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));

            return new KeyPair(publicKey, privateKey);
        }

        // Prevents startup when either required JWT key is missing.
        throw new IllegalArgumentException("Privat eller publik nyckel för JWT saknas");
    }

    // Exposes the RSA keys as a JWK source for JWT signing and verification.
    @Bean
    public JWKSource<SecurityContext> jwkSource(KeyPair keyPair) {
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(jwtKeyId)
                .build();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    // Creates the encoder used to sign JWTs with the configured RSA private key.
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    // Creates a decoder that verifies JWT signatures using the RSA public key.
    @Bean
    public JwtDecoder jwtDecoder(KeyPair keyPair) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(
                (RSAPublicKey) keyPair.getPublic()
        ).build();

        // Validates the token's standard claims, including its configured issuer.
        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(jwtIssuer)
        );

        return decoder;
    }

    // Provides BCrypt hashing for securely storing and verifying passwords.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Retrieves Spring Security's authentication manager for credential authentication.
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Configures allowed frontend origins, request headers, methods, and credentials.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allows requests from the local frontend development servers.
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:5174");
        config.addAllowedOrigin("http://localhost:5175");

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        // Allows cross-origin requests to include credentials, such as cookies.
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // Applies the CORS configuration to every endpoint.
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
