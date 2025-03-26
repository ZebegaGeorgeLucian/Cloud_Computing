package com.example.HW2Application.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String credentials;

    public FirebaseAuthenticationToken(Object principal, String credentials) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        this.principal = principal;
        this.credentials = credentials;
        // Mark token as authenticated.
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
