package com.stage.gestion_stages.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Désactiver CSRF (car on utilise JWT)

                .authorizeHttpRequests(auth -> auth
                        // Routes publiques (accessibles sans authentification)
                        .requestMatchers("/api/auth/**").permitAll() // Login, Register
                        .requestMatchers(HttpMethod.GET, "/api/offers").permitAll() // Voir les offres (public)
                        .requestMatchers(HttpMethod.GET, "/api/offers/**").permitAll() // Détails d'une offre (public)

                        // Routes protégées par rôle
                        .requestMatchers(HttpMethod.POST, "/api/offers").hasRole("ENTREPRISE") // Créer offre : ENTREPRISE
                        .requestMatchers(HttpMethod.PUT, "/api/offers/**").hasRole("ENTREPRISE") // Modifier offre : ENTREPRISE
                        .requestMatchers(HttpMethod.DELETE, "/api/offers/**").hasRole("ENTREPRISE") // Supprimer offre : ENTREPRISE

                        .requestMatchers(HttpMethod.POST, "/api/candidatures").hasRole("ETUDIANT") // Postuler : ETUDIANT
                        .requestMatchers(HttpMethod.GET, "/api/candidatures/etudiant/**").hasRole("ETUDIANT") // Voir ses candidatures : ETUDIANT

                        .requestMatchers(HttpMethod.GET, "/api/candidatures/offre/**").hasAnyRole("ENTREPRISE", "ADMIN") // Voir candidatures d'une offre : ENTREPRISE ou ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api/candidatures/*/statut").hasAnyRole("ENTREPRISE", "ADMIN") // Changer statut : ENTREPRISE ou ADMIN

                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN") // Liste users : ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN") // Supprimer user : ADMIN

                        // Toutes les autres routes nécessitent une authentification (peu importe le rôle)
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Pas de session (JWT)
                )

                // Ajouter le filtre JWT AVANT le filtre d'authentification par défaut
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}