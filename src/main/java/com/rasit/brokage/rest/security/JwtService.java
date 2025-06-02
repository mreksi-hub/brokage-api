package com.rasit.brokage.rest.security;

import com.rasit.brokage.core.data.entity.CustomerEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    @Value("${token.signing.key}")
    private String jwtSigningKey;

    @Value("${token.refresh.signing.key}")
    private String jwtRefreshSigningKey;

    @Value("${token.access.token.expiration}")
    private long accessTokenExpiration;

    @Value("${token.refresh.token.expiration}")
    private long refreshTokenExpiration;


    private String extractUserName(String token, String signingKey) {
        return extractClaim(token, Claims::getSubject, signingKey);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver, String signingKey) {
        final Claims claims = extractAllClaims(token, signingKey);
        return claimsResolver.apply(claims);
    }

    public String generateToken(CustomerEntity customer) {
        return Jwts.builder().setSubject(customer.getUsername()).claim("roles", customer.getAuthorities()).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration)).signWith(getSignInKey(jwtSigningKey), SignatureAlgorithm.HS256).compact();
    }

    private Claims extractAllClaims(String token, String signingKey) {
        return Jwts.parser().setSigningKey(getSignInKey(signingKey)).build().parseClaimsJws(token).getBody();
    }

    private Key getSignInKey(String signingKey) {
        byte[] keyBytes = Decoders.BASE64.decode(signingKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateRefresh(Map<String, Objects> extraClaims, UserDetails userDetails) {
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername()).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration)).signWith(getSignInKey(jwtRefreshSigningKey), SignatureAlgorithm.HS256).compact();
    }

    public String getUsernameFromToken(String token) {
        return getUsernameFromToken(token, jwtSigningKey);
    }

    public String getUsernameFromRefreshToken(String token) {
        return getUsernameFromToken(token, jwtRefreshSigningKey);
    }

    private String getUsernameFromToken(String token, String signingKey) {
        return extractUserName(token, signingKey);
    }

    public boolean validateToken(String token) {
        return validateToken(token, jwtSigningKey);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, jwtRefreshSigningKey);
    }

    private boolean validateToken(String token, String signingKey) {
        try {
            Jwts.parser().setSigningKey(getSignInKey(signingKey)).build().parse(token);
            return true;
        } catch (SignatureException e) {
            log.error("JWT signature does not match locally computed signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
