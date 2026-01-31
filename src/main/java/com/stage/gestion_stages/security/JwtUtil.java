package com.stage.gestion_stages.security;

import io.jsonwebtoken.Claims;//les données contenues dans le token (payload)
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component; //Permet à Spring de gérer cette classe automatiquement(avec @Autowired)

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;//stocker des infos dans le token
import java.util.Map;
import java.util.function.Function; //fonction Java utilisée pour extraire des données du token

@Component
public class JwtUtil {

    // Clé secrète pour signer les tokens
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Durée de validité du token : 24 heures (en millisecondes)
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24h

    // Extraire le username (email) du token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extraire une information spécifique du token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extraire toutes les informations du token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Générer un token pour un utilisateur
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // Ajouter le rôle dans le token
        return createToken(claims, username);
    }

    // Créer le token JWT
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims) // Informations personnalisées (role)
                .setSubject(subject) // L'email de l'utilisateur
                .setIssuedAt(new Date(System.currentTimeMillis())) // Date de création
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Date d'expiration
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // Signature avec la clé secrète
                .compact(); //transforme tout en String JWT
    }

    // Valider le token (vérifier qu'il correspond à l'utilisateur et n'est pas expiré)
    // Cette validation est utilisée par le filtre de sécurité avant d'autoriser l'accès aux endpoints protégés
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }


    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }
}