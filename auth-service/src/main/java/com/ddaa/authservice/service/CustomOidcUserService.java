package com.ddaa.authservice.service;

import com.ddaa.authservice.exception.UnauthorizedDomainException;
import com.ddaa.authservice.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserService userService;

    @Value("${app.security.allowed-google-domain}")
    private String allowedGoogleDomain;

    public CustomOidcUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String googleId = oidcUser.getSubject();
        String name = oidcUser.getFullName();
        String email = oidcUser.getEmail();
        String hostedDomain = oidcUser.getAttribute("hd");
        Boolean emailVerified = oidcUser.getEmailVerified();

        if (email == null || googleId == null) {
            throw new UnauthorizedDomainException("Missing required Google account data");
        }

        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new UnauthorizedDomainException("Google email is not verified");
        }

        boolean validDomain =
                email.toLowerCase().endsWith("@" + allowedGoogleDomain.toLowerCase()) &&
                        allowedGoogleDomain.equalsIgnoreCase(hostedDomain);

        if (!validDomain) {
            throw new UnauthorizedDomainException("Unauthorized Google Workspace domain");
        }

        User user = userService.createOrUpdateGoogleUser(googleId, name, email);

        return new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "email"
        );
    }
}