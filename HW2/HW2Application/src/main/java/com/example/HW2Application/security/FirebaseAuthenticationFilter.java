package com.example.HW2Application.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Bypass token verification for public endpoints (e.g., /weather/**)
        String uri = request.getRequestURI();
        if (uri.startsWith("/weather/")) {
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");
        System.out.println("FirebaseAuthenticationFilter: Authorization header = " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.substring(7);
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                System.out.println("Token verified. Firebase UID: " + decodedToken.getUid());
                FirebaseAuthenticationToken authentication =
                        new FirebaseAuthenticationToken(decodedToken.getUid(), idToken);
                System.out.println("Authentication created with roles: " + authentication.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                System.err.println("Token verification failed: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired Firebase token");
                return;
            }
        } else {
            System.out.println("No valid Authorization header present");
        }
        filterChain.doFilter(request, response);
    }

}
