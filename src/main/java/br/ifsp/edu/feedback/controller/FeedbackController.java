package br.ifsp.edu.feedback.controller;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
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
import br.ifsp.edu.feedback.service.ExportService;
import br.ifsp.edu.feedback.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;



@AllArgsConstructor
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
	
	private final FeedbackService feedbackService;
	private final ExportService exportService;
	
	//MAPPINGS(EMPLOYEES)
	
	//HISTÓRIA DE USUÁRIO 5 - RETORNAR APENAS MEUS FEEDBACKS NÃO ANONIMOS
	@Operation(
		    summary = "Listar feedbacks não anônimos do usuário logado",
		    description = "Retorna todos os feedbacks visíveis (não anônimos) enviados ao usuário autenticado.",
		    security = @SecurityRequirement(name = "bearerAuth")
		)
		@ApiResponses(value = {
		    @ApiResponse(responseCode = "200", description = "Feedbacks retornados com sucesso"),
		    @ApiResponse(responseCode = "401", description = "Não autorizado")
		})
	@GetMapping("/me")
	@PreAuthorize("hasRole('EMPLOYEE')")
	public ResponseEntity<List<FeedbackResponseDTO>> returnMyFeedbacks(@AuthenticationPrincipal UserAuthenticated authentication) {
	    List<FeedbackResponseDTO> response = feedbackService.getNonAnonymousFeedbacksByUser(authentication.getUser());
	    return ResponseEntity.ok(response);
	}
	
	//HISTÓRIA DE USUÁRIO 4 - ENVIAR FEEDBACK
	@Operation(
		    summary = "Enviar feedback",
		    description = "Permite que um funcionário envie um feedback.",
		    security = @SecurityRequirement(name = "bearerAuth")
		)
		@ApiResponses(value = {
		    @ApiResponse(responseCode = "200", description = "Feedback criado com sucesso",
		        content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedbackResponseDTO.class))),
		    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
		    @ApiResponse(responseCode = "401", description = "Não autorizado")
		})
	@SecurityRequirement(name = "bearerAuth")
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
	@Operation(
		    summary = "Listar feedbacks com filtros",
		    description = "Lista todos os feedbacks com filtros opcionais por setor, data inicial e data final.",
		    security = @SecurityRequirement(name = "bearerAuth")
		)
		@ApiResponses(value = {
		    @ApiResponse(responseCode = "200", description = "Feedbacks filtrados retornados com sucesso"),
		    @ApiResponse(responseCode = "401", description = "Não autorizado")
		})
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/filter")
		public ResponseEntity<List<FeedbackResponseDTO>> filterFeedbacks(
				@Parameter(description = "Nome do setor") @RequestParam(required = false) String setor,
				@Parameter(description = "Data inicial (formato: yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
				@Parameter(description = "Data final (formato: yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {

			List<FeedbackResponseDTO> response = feedbackService.getFilteredFeedbacks(setor, data, dataFinal);
			return ResponseEntity.ok(response);
	}

	
	
	//HISTÓRIA DE USUÁRIO 3 -  acesso ao conteúdo de todos os feedbacks enviados
	@Operation(
		    summary = "Listar todos os feedbacks",
		    description = "Retorna todos os feedbacks enviados. Acesso restrito a administradores.",
		    security = @SecurityRequirement(name = "bearerAuth")
		)
		@ApiResponses(value = {
		    @ApiResponse(responseCode = "200", description = "Lista de feedbacks retornada com sucesso"),
		    @ApiResponse(responseCode = "401", description = "Não autorizado")
		})
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping()
	public ResponseEntity<List<FeedbackResponseDTO>> getAllFeedbacksAdmin() {
	    List<FeedbackResponseDTO> response = feedbackService.getAllFeedbacks();
	    return ResponseEntity.ok(response);
	}
	
	
	//HISTÓRIA DE USUÁRIO 8 - excluir feedbacks 
	@Operation(
		    summary = "Excluir feedback",
		    description = "Exclui um feedback com base no ID. Acesso restrito a administradores.",
		    security = @SecurityRequirement(name = "bearerAuth")
		)
		@ApiResponses(value = {
		    @ApiResponse(responseCode = "204", description = "Feedback excluído com sucesso"),
		    @ApiResponse(responseCode = "403", description = "Não autorizado"),
		    @ApiResponse(responseCode = "401", description = "Feedback não encontrado")
		})
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteFeedbackById(@PathVariable("id") Long feedbackId) {
		 feedbackService.deleteFeedback(feedbackId);
		 return ResponseEntity.noContent().build();
	}
	
	
	//HISTÓRIA DE USUÁRIO 11 - agrupar por tipo
	@Operation(
		    summary = "Agrupar feedbacks por tipo",
		    description = "Agrupa os feedbacks com base no tipo (Elogio, Crítica, Sugestão, etc). Acesso restrito a administradores.",
		    security = @SecurityRequirement(name = "bearerAuth")
		)
		@ApiResponses(value = {
		    @ApiResponse(responseCode = "200", description = "Feedbacks agrupados por tipo retornados com sucesso"),
		    @ApiResponse(responseCode = "401", description = "Não autorizado")
		})
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/grouped-by-type")
	public ResponseEntity<Map<FeedbackType, List<FeedbackResponseDTO>>>groupFeedbacksByType(){
		var grouped = feedbackService.getFeedbacksGroupedByType();
		return ResponseEntity.ok(grouped);
	}
	
	
	//HISTÓRIA DE USUÁRIO 10 - exportar feedbacks para .csv
	@Operation(
		    summary = "Exportar feedbacks para CSV",
		    description = "Exporta todos os feedbacks para um arquivo CSV salvo na pasta 'exports'. Acesso restrito a administradores.",
		    security = @SecurityRequirement(name = "bearerAuth")
		)
		@ApiResponses(value = {
		    @ApiResponse(responseCode = "200", description = "Feedbacks exportados com sucesso"),
		    @ApiResponse(responseCode = "401", description = "Não autorizado")
		})
		@PreAuthorize("hasRole('ADMIN')")
		@GetMapping("/export")
		public ResponseEntity<String> exportFeedbacksToCSV() {
		 try {
	            Path filePath = exportService.exportFeedbacksToCsvFile();
	            return ResponseEntity.ok("Feedbacks exportados com sucesso para: " + filePath.toAbsolutePath());
	        } catch (Exception e) {
	            return ResponseEntity.internalServerError().body("Erro ao exportar feedbacks: " + e.getMessage());
	        }
		}
	
	@GetMapping("/export/download")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Resource> downloadFeedbackCsv() throws IOException {
	    Path filePath = Paths.get("exports/feedbacks-export.csv");
	    Resource resource = new UrlResource(filePath.toUri());

	    if (!resource.exists()) {
	        return ResponseEntity.notFound().build();
	    }

	    return ResponseEntity.ok()
	        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=feedbacks-export.csv")
	        .contentType(MediaType.parseMediaType("text/csv"))
	        .body(resource);
	}
}
