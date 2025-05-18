package br.ifsp.edu.feedback.dto.feedback;

import java.time.LocalDateTime;
import java.util.List;

import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackResponseDTO {
    private Long id;
    private String content;
    private String sector;
    private FeedbackType type;
    private boolean anonymous;
    private LocalDateTime createdAt;
    private List<String> tags;
    private String authorName;
}
