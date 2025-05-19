package br.ifsp.edu.feedback.dto.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO de requisição para autenticação")
public class AuthenticationDTO {
    @NotBlank(message = "Please, enter your username.")
    private String username;
    @NotBlank(message = "Please, enter your password.")
    private String password;
}
