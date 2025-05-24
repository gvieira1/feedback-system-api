package br.ifsp.edu.feedback.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.edu.feedback.dto.authentication.UserRegistrationDTO;
import br.ifsp.edu.feedback.dto.user.UserDTO;
import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.model.enumerations.Role;
import br.ifsp.edu.feedback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Registro público de usuário", description = "Permite que qualquer pessoa se registre como funcionário (EMPLOYEE)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping("/public-register")
    public ResponseEntity<User> publicRegister(@RequestBody @Valid UserRegistrationDTO userDto) {
    	userDto.setRole(Role.EMPLOYEE);
        User user = userService.registerUser(userDto);
        return ResponseEntity.ok(user);
    }

    @Operation(
        summary = "Registrar novo usuário (admin)",
        description = "Permite que um administrador crie novos usuários com qualquer role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Acesso negado – apenas administradores")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin-register")
    public ResponseEntity<User> adminRegister(@RequestBody UserRegistrationDTO userDto) {
        User user = userService.registerUser(userDto);
        return ResponseEntity.ok(user);
    }

	@Operation(
		    summary = "Lista todos os usuários registrados",
		    description = "Permite listar todos os usuários cadastrados. Acesso restrito a administradores.",
		    security = @SecurityRequirement(name = "bearerAuth")
		)
		@ApiResponses(value = {
		    @ApiResponse(responseCode = "200", description = "Usuários cadastrados retornados com sucesso"),
		    @ApiResponse(responseCode = "401", description = "Não autorizado")
		})
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsersDTO();
        return ResponseEntity.ok(users);
    }
}
