package com.thiru.investment_tracker.auth.service;

import com.thiru.investment_tracker.auth.entity.UserDetail;
import com.thiru.investment_tracker.auth.model.RegistrationRequest;
import com.thiru.investment_tracker.auth.repository.UserDetailsRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
@AllArgsConstructor
public class AuthService {

    private final UserDetailsRepository userDetailsRepo;
    private final PasswordEncoder passwordEncoder;

    public UserDetails getUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDetail> optionalUserDetails = userDetailsRepo.findById(username);
        return optionalUserDetails.map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found " + username));
    }

    public String addUser(RegistrationRequest request) {

        UserDetail userEntity = new UserDetail();
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        StringBuilder roles = new StringBuilder();
        for (String role: request.getRoles().split(",")) {
            roles.append("ROLE_").append(role).append(",");
        }
        userEntity.setRoles(roles.toString());
        userDetailsRepo.save(userEntity);
        return "user with username " + request.getEmail() + " added to system ";
    }

    /**
     * <a href="https://www.grc.com/passwords.htm">Token Website</a>
     */
    final private static String SECRET = "80DC002A54B59ACF5F198D0A8D644EEE992C04FFBCF947DAAA90AA7DFDDA2A05";

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public Boolean validateToken(Claims claims, UserDetails userDetails) {
        final String username = claims.getSubject();
        return username.equals(userDetails.getUsername()) && !isTokenExpired(claims);
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();

        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*30))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}