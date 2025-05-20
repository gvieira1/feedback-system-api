package br.ifsp.edu.feedback.dto.feedback;

import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackStatsDTO {
	private long totalFeedbacks;
	private String topSector;
	private long topSectorCount;
	private FeedbackType mostFrequentType;
	private long mostFrequentTypeCount;
}