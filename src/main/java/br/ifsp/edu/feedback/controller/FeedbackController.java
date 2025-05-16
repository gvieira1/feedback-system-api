package br.ifsp.edu.feedback.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.edu.feedback.model.Feedback;
import br.ifsp.edu.feedback.model.UserAuthenticated;
import br.ifsp.edu.feedback.service.FeedbackService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
	
	private final FeedbackService feedbackService;
	
	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping
	public ResponseEntity<List<Feedback>> returnFeedbacksByUser(@AuthenticationPrincipal UserAuthenticated authentication){
		return ResponseEntity.ok(feedbackService.getFeedbacksByUser(authentication.getUser()));
	}
}
