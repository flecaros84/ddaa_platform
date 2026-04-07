package com.ddaa.authservice.service;

import com.ddaa.authservice.exception.UnauthorizedDomainException;
import com.ddaa.authservice.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Value("${app.security.allowed-google-domain}")
    private String allowedGoogleDomain;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = delegate.loadUser(userRequest);
        Map<String, Object> attributes = oauthUser.getAttributes();

        String googleId = (String) attributes.get("sub");
        String name = (String) attributes.get("name");
        String email = (String) attributes.get("email");
        String hostedDomain = (String) attributes.get("hd");
        Boolean emailVerified = (Boolean) attributes.get("email_verified");

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

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                attributes,
                "email"
        );
    }
}