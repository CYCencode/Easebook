package evelyn.site.socialmedia.security;

import evelyn.site.socialmedia.model.Users;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Log4j2
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpirationInMillis;

    // Method to generate JWT token
    public String createToken(Users user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMillis);

        return Jwts.builder()
                .setSubject(user.getId())
                .claim("email", user.getEmail())
//                .claim("name", user.getName())
                .claim("id", user.getId())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public Map<String, Object> getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token);
            //log.info("JWT token is valid");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported");
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed");
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature");
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty");
        }
        return false;
    }

}

