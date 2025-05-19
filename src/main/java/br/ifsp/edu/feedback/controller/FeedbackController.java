package br.ifsp.edu.feedback.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.edu.feedback.dto.feedback.FeedbackRequestDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackResponseDTO;
import br.ifsp.edu.feedback.model.UserAuthenticated;
import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
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
	
	
	//HISTÓRIA DE USUÁRIO 5 - RETORNAR APENAS MEUS FEEDBACKS NÃO ANONIMOS
	@GetMapping("/me")
	@PreAuthorize("hasRole('EMPLOYEE')")
	public ResponseEntity<List<FeedbackResponseDTO>> returnMyFeedbacks(@AuthenticationPrincipal UserAuthenticated authentication) {
	    List<FeedbackResponseDTO> response = feedbackService.getNonAnonymousFeedbacksByUser(authentication.getUser());
	    return ResponseEntity.ok(response);
	}
	
	//HISTÓRIA DE USUÁRIO 4 - ENVIAR FEEDBACK
	@PostMapping
	@PreAuthorize("hasRole('EMPLOYEE')")
	public ResponseEntity<FeedbackResponseDTO> createFeedback(
	        @RequestBody @Valid FeedbackRequestDTO feedbackDTO,
	        @AuthenticationPrincipal UserAuthenticated authentication) {
	    
		FeedbackResponseDTO feedback = feedbackService.createFeedback(feedbackDTO, authentication.getUser());
	    return ResponseEntity.ok(feedback);
	}
	
	//MAPPINGS(ADMINS)
	
	//HISTÓRIA DE USUÁRIO 7 -  visualizar todos os feedbacks enviados, com filtros por setor e data 
	//HISTÓRIA DE USUÁRIO 9 - gerar relatórios com base nos feedbacks recebidos por período e setor
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/filter")
	public ResponseEntity<List<FeedbackResponseDTO>> filterFeedbacks(
	        @RequestParam(required = false) String setor,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal){

	    List<FeedbackResponseDTO> response = feedbackService.getFilteredFeedbacks(setor, data, dataFinal);
	    return ResponseEntity.ok(response);
	}
	
	//HISTÓRIA DE USUÁRIO 3 -  acesso ao conteúdo de todos os feedbacks enviados
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping()
	public ResponseEntity<List<FeedbackResponseDTO>> getAllFeedbacksAdmin() {
	    List<FeedbackResponseDTO> response = feedbackService.getAllFeedbacks();
	    return ResponseEntity.ok(response);
	}
	
	//HISTÓRIA DE USUÁRIO 8 - excluir feedbacks 
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteFeedbackById(@PathVariable("id") Long feedbackId) {
		 feedbackService.deleteFeedback(feedbackId);
		 return ResponseEntity.noContent().build();
	}
	
	//HISTÓRIA DE USUÁRIO 11 - agrupar por tipo
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/grouped-by-type")
	public ResponseEntity<Map<FeedbackType, List<FeedbackResponseDTO>>>groupFeedbacksByType(){
		var grouped = feedbackService.getFeedbacksGroupedByType();
		return ResponseEntity.ok(grouped);
	}
}
