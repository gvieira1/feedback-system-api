package br.ifsp.edu.feedback.dto.feedback;

import java.util.List;

import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "DTO de requisição para envio de feedback")
public class FeedbackRequestDTO {
	@NotBlank(message = "Title is required")
	private String titulo;
	
    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Sector is required")
    private String sector;

    @NotNull(message = "Type is required")
    private FeedbackType type;

    private boolean anonymous;

    private List<String> tags;
}
