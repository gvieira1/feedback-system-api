package br.ifsp.edu.feedback.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.ifsp.edu.feedback.dto.feedback.FeedbackRequestDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackResponseDTO;
import br.ifsp.edu.feedback.model.Feedback;
import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.repository.FeedbackRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class FeedbackService {
	
	private final FeedbackRepository feedbackRepository;
	
	public List<Feedback> getMyFeedbacks(User user) {
		return feedbackRepository.findByAuthor(user);
	}
	
	
	public Feedback createFeedback(FeedbackRequestDTO dto, User author) {
	    Feedback feedback = Feedback.builder()
	            .content(dto.getContent())
	            .sector(dto.getSector())
	            .type(dto.getType())
	            .anonymous(dto.isAnonymous())
	            .tags(dto.getTags())
	            .author(author)
	            .build();

	    return feedbackRepository.save(feedback);
	}
	
	
	public FeedbackResponseDTO toResponseDTO(Feedback feedback) {
	    return FeedbackResponseDTO.builder()
	            .id(feedback.getId())
	            .content(feedback.getContent())
	            .sector(feedback.getSector())
	            .type(feedback.getType())
	            .anonymous(feedback.isAnonymous())
	            .createdAt(feedback.getCreatedAt())
	            .tags(feedback.getTags())
	            .authorName(feedback.isAnonymous() ? null : feedback.getAuthor().getUsername())
	            .build();
	}
	
	public List<Feedback> filterFeedbacks(String setor, LocalDate data) {
	    if (setor != null && data != null) {
	        return feedbackRepository.findBySectorAndCreatedAtBetween(setor, data.atStartOfDay(), data.atTime(LocalTime.MAX));
	    } else if (setor != null) {
	        return feedbackRepository.findBySector(setor);
	    } else if (data != null) {
	        return feedbackRepository.findByCreatedAtBetween(data.atStartOfDay(), data.atTime(LocalTime.MAX));
	    } else {
	        return feedbackRepository.findAll();
	    }
	}
}
