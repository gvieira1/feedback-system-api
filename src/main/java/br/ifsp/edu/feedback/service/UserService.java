package br.ifsp.edu.feedback.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.ifsp.edu.feedback.dto.authentication.UserRegistrationDTO;
import br.ifsp.edu.feedback.dto.user.UserDTO;
import br.ifsp.edu.feedback.exception.UserAlreadyExistsException;
import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.model.enumerations.Role;
import br.ifsp.edu.feedback.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // REGISTRA NOVO USUÁRIO
    public User registerUser(UserRegistrationDTO userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }        
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }
        if (!userDto.getPassword().equals(userDto.getPasswordConfirmation())) {
            throw new IllegalArgumentException("Password and confirmation do not match");
        }

        if (Role.ADMIN.equals(userDto.getRole())) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getAuthorities().stream()
                    .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                throw new AccessDeniedException("Somente administradores podem criar usuários com role ADMIN");
            }
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(userDto.getRole());

        return userRepository.save(user);
    }

    // RETORNA TODOS OS USUÁRIOS EM FORMATO DTO
    public List<UserDTO> getAllUsersDTO() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole()))
                .collect(Collectors.toList());
    }
}
