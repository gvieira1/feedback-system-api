package br.ifsp.edu.feedback.dto.feedback;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
	private Map<String, Long> feedbacksBySector;
	private Map<String, Long> feedbacksByType;
	private long anonymousCount;
	private long nonAnonymousCount;
}
