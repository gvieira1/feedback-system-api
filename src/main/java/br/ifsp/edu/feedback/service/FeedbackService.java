package br.ifsp.edu.feedback.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.ifsp.edu.feedback.dto.feedback.FeedbackRequestDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackResponseDTO;
import br.ifsp.edu.feedback.exception.ResourceNotFoundException;
import br.ifsp.edu.feedback.model.Feedback;
import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import br.ifsp.edu.feedback.repository.FeedbackRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class FeedbackService {

	private final FeedbackRepository feedbackRepository;

	//BUSCA FEEDBACKS NÂO ANONIMOS DO FUNCIONARIO E DEVOLVE DTO
	public List<FeedbackResponseDTO> getNonAnonymousFeedbacksByUser(User user) {
	    return feedbackRepository.findByAuthor(user).stream()
	            .filter(fb -> !fb.isAnonymous())
	            .map(this::toResponseDTO)
	            .toList();
	}

	//BUSCA TODOS OS FEEDBACKS PARA O ADMIN E DEVOLVE DTO
	public List<FeedbackResponseDTO> getAllFeedbacks() {
	    return feedbackRepository.findAll().stream()
	            .map(this::toResponseDTO)
	            .toList();
	}
	
	//BUSCA LISTA SIMPLES DE FEEDBACKS
	public List<Feedback> getAllFeedbacksInListFormat(){
		return feedbackRepository.findAll();
	}

	//MONTA FEEDBACK E RETORNA DTO
	public FeedbackResponseDTO createFeedback(FeedbackRequestDTO dto, User author) {
	    Feedback feedback = Feedback.builder()
	            .titulo(dto.getTitulo())
	            .content(dto.getContent())
	            .sector(dto.getSector())
	            .type(dto.getType())
	            .anonymous(dto.isAnonymous())
	            .tags(dto.getTags())
	            .author(author)
	            .build();

	    Feedback savedFeedback = feedbackRepository.save(feedback);
	    return toResponseDTO(savedFeedback);
	}

	//CONVERTE PARA DTO
	public FeedbackResponseDTO toResponseDTO(Feedback feedback) {
	    return FeedbackResponseDTO.builder()
	            .id(feedback.getId())
	            .titulo(feedback.getTitulo())
	            .content(feedback.getContent())
	            .sector(feedback.getSector())
	            .type(feedback.getType())
	            .anonymous(feedback.isAnonymous())
	            .createdAt(feedback.getCreatedAt())
	            .tags(feedback.getTags())
	            .authorName(feedback.isAnonymous() ? null : feedback.getAuthor().getUsername())
	            .build();
	}

	//RESPONDE CONTROLLER E DEVOLVE DTO
	public List<FeedbackResponseDTO> getFilteredFeedbacks(String setor, LocalDate data, LocalDate dataFinal) {
	    List<Feedback> feedbacks = findFeedbacks(setor, data, dataFinal);
	    return feedbacks.stream()
	            .map(this::toResponseDTO)
	            .toList();
	}

	//BUSCA NO REPOSITÓRIO
	public List<Feedback> findFeedbacks(String setor, LocalDate dataInicial, LocalDate dataFinal) {
	    boolean temSetor = setor != null && !setor.isBlank();
	    boolean temDataInicial = dataInicial != null;
	    boolean temDataFinal = dataFinal != null;

	    if (temSetor && temDataInicial && temDataFinal) {
	        return feedbackRepository.findBySectorAndCreatedAtBetween(
	            setor, startOfDay(dataInicial), endOfDay(dataFinal)
	        );
	    }
	    if (temSetor && temDataInicial) {
	        return feedbackRepository.findBySectorAndCreatedAtBetween(
	            setor, startOfDay(dataInicial), endOfDay(dataInicial)
	        );
	    }
	    if (temSetor) {
	        return feedbackRepository.findBySector(setor);
	    }
	    if (temDataInicial) {
	        return feedbackRepository.findByCreatedAtBetween(
	            startOfDay(dataInicial), endOfDay(dataInicial)
	        );
	    }
	    return feedbackRepository.findAll();
	}

	//FUNÇOES AUXILIARES DE DATA
	private LocalDateTime startOfDay(LocalDate date) {
	    return date.atStartOfDay();
	}

	private LocalDateTime endOfDay(LocalDate date) {
	    return date.atTime(LocalTime.MAX);
	}

	//DELETA FEEDBACK POR ID
	public void deleteFeedback(Long feedbackId) {
		Feedback existingFeedback = feedbackRepository.findById(feedbackId)
				.orElseThrow(() -> new ResourceNotFoundException("Feedback não encontrado!"));
		feedbackRepository.delete(existingFeedback);
	}

	//RETORNA LISTA DE FEEDBACKS AGRUPADOS POR TIPO 
	public Map<FeedbackType, List<FeedbackResponseDTO>> getFeedbacksGroupedByType() {
	    List<Feedback> feedbacks = feedbackRepository.findAll();
	    return feedbacks.stream()
	            .map(this::toResponseDTO)
	            .collect(Collectors.groupingBy(FeedbackResponseDTO::getType));
	}



}