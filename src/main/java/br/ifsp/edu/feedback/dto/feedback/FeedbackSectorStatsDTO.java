package br.ifsp.edu.feedback.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeedbackSectorStatsDTO {
    private String sector;
    private long count;
}
