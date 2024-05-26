package com.thiru.investment_tracker.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thiru.investment_tracker.auth.model.LoginRequest;
import com.thiru.investment_tracker.auth.model.LoginResponse;
import com.thiru.investment_tracker.auth.model.RegistrationRequest;
import com.thiru.investment_tracker.auth.service.AuthService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(auth);
        if (authentication.isAuthenticated()) {
            String token = authService.generateToken(loginRequest.getEmail());
            LoginResponse loginResponse = LoginResponse.from(token);
            return ResponseEntity.ok(loginResponse);
        } else {
            throw new UsernameNotFoundException("Invalid user request");
        }
    }

    @PostMapping("/register")
    public String addNewUser(@RequestBody RegistrationRequest request){
        return authService.addUser(request);
    }

    @GetMapping("/login")
    public String login1() {
        return "login";
    }
}
