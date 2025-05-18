package br.ifsp.edu.feedback.service;

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
	
	public List<Feedback> getFeedbacksByUser(User user) {
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
}
