package se.iths.oscarp.microprojectauthserver.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String COOKIE_NAME = "accessToken";

    private final DefaultBearerTokenResolver defaultResolver =
            new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {

        // First check the normal Authorization header
        String token = defaultResolver.resolve(request);

        if (token != null) {
            return token;
        }

        // Otherwise look for the JWT in the cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
