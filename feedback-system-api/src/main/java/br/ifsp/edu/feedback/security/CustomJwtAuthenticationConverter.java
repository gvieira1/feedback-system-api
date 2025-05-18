package br.ifsp.edu.feedback.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;

import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.model.UserAuthenticated;
import br.ifsp.edu.feedback.repository.UserRepository;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    
	@Autowired
    private UserRepository userRepository;
    
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UserAuthenticated userAuthenticated = extractUser(jwt);
        List<GrantedAuthority> authorities = List.copyOf(userAuthenticated.getAuthorities());
        return new UsernamePasswordAuthenticationToken(userAuthenticated, null, authorities);
    }
    
    private UserAuthenticated extractUser(Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return new UserAuthenticated(user);
    }
}