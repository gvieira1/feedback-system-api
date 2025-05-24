package br.ifsp.edu.feedback.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.edu.feedback.dto.feedback.DashboardStatsDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackSectorStatsDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackStatsDTO;
import br.ifsp.edu.feedback.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/reports")
public class ReportController {

		private final ReportService reportService;
		
		
		//HISTÓRIA DE USUÁRIO 9.1 - gerar relatórios (contagem de feedbacks por setor, top x setores com mais feedbacks)
		@Operation(
			    summary = "Listar setores com mais feedbacks",
			    description = "Retorna os setores com maior número de feedbacks, limitado a um número específico.",
			    security = @SecurityRequirement(name = "bearerAuth")
			)
			@ApiResponses(value = {
			    @ApiResponse(responseCode = "200", description = "Setores retornados com sucesso"),
			    @ApiResponse(responseCode = "401", description = "Não autorizado")
			})
			@PreAuthorize("hasRole('ADMIN')")
			@GetMapping("/top-sectors")
			public ResponseEntity<List<FeedbackSectorStatsDTO>> getTopSectors(
			        @RequestParam(defaultValue = "3") int limit) {
			    List<FeedbackSectorStatsDTO> topSectors = reportService.getTopSectorsWithMostFeedbacks(limit);
			    return ResponseEntity.ok(topSectors);
			}
		
		
		//HISTÓRIA DE USUÁRIO 9.2 - gerar relatórios (contagem de feedbacks nos ultimos 3 meses, contagem tipo com mais feedbacks, setor com mais feedbacks + contagem)
		@Operation(
			    summary = "Estatísticas dos feedbacks (últimos 3 meses)",
			    description = "Listar total de feedbacks, setor mais ativo e tipo de feedback mais comum nos últimos 3 meses.",
			    security = @SecurityRequirement(name = "bearerAuth")
		)
		@ApiResponses(value = {
	    @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso"),
		@ApiResponse(responseCode = "401", description = "Não autorizado")
		})
		@PreAuthorize("hasRole('ADMIN')")
		@GetMapping("/last-three-months")
		public ResponseEntity<FeedbackStatsDTO> getLastThreeMonthsStats() {
			 FeedbackStatsDTO stats = reportService.getLastThreeMonthsStats();
			 return ResponseEntity.ok(stats);
		}
		
		
		//HISTÓRIA DE USUÁRIO EXTRA - Dashboard simples com estatísticas dos feedbacks 
		@Operation(
			summary = "Retorna DTO de Dashboard",
			description = "Retorna contagem de feedbacks por setor, tipo e contagem de anonimos vs não anonimos",
			security = @SecurityRequirement(name = "bearerAuth")
		)
		@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Dados dashboard retornados com sucesso"),
		@ApiResponse(responseCode = "401", description = "Não autorizado")
		})
		@PreAuthorize("hasRole('ADMIN')")		
		@GetMapping("/dashboard")
		public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
			return ResponseEntity.ok(reportService.getDashboardStats());
		}
}

