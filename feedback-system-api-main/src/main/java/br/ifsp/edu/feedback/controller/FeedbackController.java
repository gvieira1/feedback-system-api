package br.ifsp.edu.feedback.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.edu.feedback.dto.feedback.FeedbackRequestDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackResponseDTO;
import br.ifsp.edu.feedback.model.Feedback;
import br.ifsp.edu.feedback.model.UserAuthenticated;
import br.ifsp.edu.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
	
	private final FeedbackService feedbackService;
	
	//MAPPINGS(EMPLOYEES)
	
	//Retornar meus feedbacks inclusive anônimos.
	/*
	@GetMapping
	@PreAuthorize("hasRole('EMPLOYEE')")
	public ResponseEntity<List<FeedbackResponseDTO>> returnMyFeedbacks(@AuthenticationPrincipal UserAuthenticated authentication){
	    List<Feedback> feedbacks = feedbackService.getMyFeedbacks(authentication.getUser());
	    List<FeedbackResponseDTO> responseDTOs = feedbacks.stream()
	        .map(feedbackService::toResponseDTO)
	        .toList();
	    return ResponseEntity.ok(responseDTOs);
	}
	FIM*/
	
	
	//Retornar apenas meus feedbacks não anônimos.
	@GetMapping
	@PreAuthorize("hasRole('EMPLOYEE')")
	public ResponseEntity<List<FeedbackResponseDTO>> returnMyFeedbacks(@AuthenticationPrincipal UserAuthenticated authentication){
	    List<Feedback> feedbacks = feedbackService.getMyFeedbacks(authentication.getUser())
	        .stream()
	        .filter(fb -> !fb.isAnonymous())
	        .toList();
	    
	    List<FeedbackResponseDTO> responseDTOs = feedbacks.stream()
	        .map(feedbackService::toResponseDTO)
	        .toList();
	    
	    return ResponseEntity.ok(responseDTOs);
	}
	//FIM
	
	
	@PostMapping
	@PreAuthorize("hasRole('EMPLOYEE')")
	public ResponseEntity<FeedbackResponseDTO> createFeedback(
	        @RequestBody @Valid FeedbackRequestDTO feedbackDTO,
	        @AuthenticationPrincipal UserAuthenticated authentication) {
	    
	    Feedback feedback = feedbackService.createFeedback(feedbackDTO, authentication.getUser());
	    FeedbackResponseDTO responseDTO = feedbackService.toResponseDTO(feedback);
	    return ResponseEntity.ok(responseDTO);
	}
	
	
	//MAPPINGS(ADMINS)
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/filter")
	public ResponseEntity<List<FeedbackResponseDTO>> filterFeedbacks(
	        @RequestParam(required = false) String setor,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

	    List<Feedback> feedbacks = feedbackService.filterFeedbacks(setor, data);
	    List<FeedbackResponseDTO> response = feedbacks.stream()
	            .map(feedbackService::toResponseDTO)
	            .toList();
	    return ResponseEntity.ok(response);
	}	
}
