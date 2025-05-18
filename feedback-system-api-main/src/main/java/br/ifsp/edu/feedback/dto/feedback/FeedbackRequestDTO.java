package br.ifsp.edu.feedback.dto.feedback;

import java.util.List;

import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequestDTO {
    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Sector is required")
    private String sector;

    @NotNull(message = "Type is required")
    private FeedbackType type;

    private boolean anonymous;

    private List<String> tags;
}
