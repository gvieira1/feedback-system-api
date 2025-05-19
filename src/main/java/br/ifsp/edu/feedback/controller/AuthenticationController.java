package br.ifsp.edu.feedback.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.edu.feedback.dto.authentication.AuthenticationDTO;
import br.ifsp.edu.feedback.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(
    	    summary = "Autentica usuário no sistema",
    	    description = "Autentica usuário no sistema com base nas credenciais fornecidas e retorna JWT token"
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Usuário válido e token gerado"),
    	    @ApiResponse(responseCode = "500", description = "Usuário inexistente ou senha inválida")
    	})
    @PostMapping("authenticate")
    public String authenticate(@RequestBody AuthenticationDTO request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        return authenticationService.authenticate(authentication);
    }
}