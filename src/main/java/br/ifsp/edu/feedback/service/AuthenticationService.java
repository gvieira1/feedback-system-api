package br.ifsp.edu.feedback.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.repository.UserRepository;

@Service
public class AuthenticationService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    
    public AuthenticationService(JwtService jwtService, UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
		this.authenticationManager = authenticationManager;
    }
    
    public String authenticate(Authentication authentication) {
    	authenticationManager.authenticate(authentication);
        String username = authentication.getName();     
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtService.generateToken(user);
    }
}