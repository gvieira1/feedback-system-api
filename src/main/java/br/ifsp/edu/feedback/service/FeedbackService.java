package br.ifsp.edu.feedback.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
}
