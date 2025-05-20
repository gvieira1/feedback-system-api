package br.ifsp.edu.feedback.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.ifsp.edu.feedback.dto.feedback.DashboardStatsDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackSectorStatsDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackStatsDTO;
import br.ifsp.edu.feedback.model.Feedback;
import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import br.ifsp.edu.feedback.repository.FeedbackRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ReportService {
	
	private final FeedbackRepository feedbackRepository;
	
	
	//ESTATISTICAS DOS ULTIMOS 3 MESES COM TOTAL DE FEEDBACKS, MAIOR QUANTIDADE POR SETOR E TIPO
	 public FeedbackStatsDTO getLastThreeMonthsStats() {
	        List<Feedback> recentFeedbacks = getFeedbacksFromLastMonths(3);

	        Map<String, Long> bySector = countBySector(recentFeedbacks);
	        Map<FeedbackType, Long> byType = countByType(recentFeedbacks);

	        String topSector = getTopKey(bySector);
	        long topSectorCount = bySector.getOrDefault(topSector, 0L);

	        FeedbackType topType = getTopKey(byType);
	        long topTypeCount = byType.getOrDefault(topType, 0L);

	        return new FeedbackStatsDTO(
	            recentFeedbacks.size(),
	            topSector,
	            topSectorCount,
	            topType,
	            topTypeCount
	        );
	    }
	 
		//DEVOLVE CONTAGEM DE FEEDBACKS POR SETOR ORDENADA E COM LIMITE DE RESPOSTA
		public List<FeedbackSectorStatsDTO> getTopSectorsWithMostFeedbacks(int limit) {
		    Map<String, Long> sectorCounts = countBySector(feedbackRepository.findAll());

		    return sectorCounts.entrySet()
		        .stream()
		        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
		        .limit(limit)
		        .map(entry -> new FeedbackSectorStatsDTO(entry.getKey(), entry.getValue()))
		        .toList();
		}

	    //MAP CHAVE/VALOR DO ULTIMO ANO COM CONTAGEM POR SETOR, TIPO, CONTAGEM DE FEEDBACKS ANONIMOS 
	    public DashboardStatsDTO getDashboardStats() {
	        List<Feedback> recentFeedbacks = getFeedbacksFromLastMonths(12);

	        Map<String, Long> bySector = countBySector(recentFeedbacks);
	        Map<String, Long> byType = countByTypeAsString(recentFeedbacks);

	        long anonymousCount = recentFeedbacks.stream()
	            .filter(Feedback::isAnonymous)
	            .count();

	        long nonAnonymousCount = recentFeedbacks.size() - anonymousCount;

	        return new DashboardStatsDTO(bySector, byType, anonymousCount, nonAnonymousCount);
	    }

	    // BUSCA FEEDBACKS DOS X MESES ANTERIORES NO BANCO 
	    private List<Feedback> getFeedbacksFromLastMonths(int months) {
	        LocalDateTime start = LocalDateTime.now().minusMonths(months);
	        return feedbackRepository.findByCreatedAtBetween(start, LocalDateTime.now());
	    }

	    //FAZ CONTAGEM DE FEEDBACKS POR SETOR
	    private Map<String, Long> countBySector(List<Feedback> feedbacks) {
	        return feedbacks.stream()
	            .collect(Collectors.groupingBy(Feedback::getSector, Collectors.counting()));
	    }

	    //FAZ CONTAGEM POR TIPO DE FEEDBACK
	    private Map<FeedbackType, Long> countByType(List<Feedback> feedbacks) {
	        return feedbacks.stream()
	            .collect(Collectors.groupingBy(Feedback::getType, Collectors.counting()));
	    }

	    //FAZ CONTAGEM POR NOME DE TIPO
	    private Map<String, Long> countByTypeAsString(List<Feedback> feedbacks) {
	        return feedbacks.stream()
	            .collect(Collectors.groupingBy(f -> f.getType().name(), Collectors.counting()));
	    }

	    //RETORNA TIPO MAIS FREQUENTE
	    private <T> T getTopKey(Map<T, Long> map) {
	        return map.entrySet().stream()
	            .max(Map.Entry.comparingByValue())
	            .map(Map.Entry::getKey)
	            .orElse(null);
	    }
}
