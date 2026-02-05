//Intercepteur qui s'exécute AVANT chaque requête HTTP.
//Il vérifie si la requête contient un token JWT valide, si oui, il authentifie l'utilisateur automatiquement.
package com.stage.gestion_stages.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Récupérer le header "Authorization"
        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Vérifier si le header existe et commence par "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Extraire le token (enlever "Bearer ")

            try {
                // Extraire l'email (username) du token
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Token invalide ou expiré
                System.out.println("Erreur lors de l'extraction du username : " + e.getMessage());
            }
        }

        // Si on a un username ET qu'il n'y a pas déjà une authentification
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Valider le token
            if (jwtUtil.validateToken(jwt, username)) {

                // Extraire le rôle du token
                String role = jwtUtil.extractRole(jwt);
                System.out.println("🔍 DEBUG - Username: " + username);
                System.out.println("🔍 DEBUG - Rôle extrait du JWT: '" + role + "'");

                // Créer une autorité Spring Security avec le rôle
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                System.out.println("🔍 DEBUG - Autorité créée: '" + authority.getAuthority() + "'");
                // Créer un objet d'authentification
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, // Principal (l'email de l'utilisateur)
                        null,     // Credentials (pas besoin avec JWT)
                        Collections.singletonList(authority) // Autorités (rôle)
                );

                // Ajouter les détails de la requête (IP, session, infos techniques)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Définir l'authentification dans le contexte Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("✅ DEBUG - Authentification réussie pour: " + username);
            } else {
                System.out.println("❌ DEBUG - Token invalide pour: " + username);

            }
        }

        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}