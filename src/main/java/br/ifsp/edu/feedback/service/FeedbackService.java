package br.ifsp.edu.feedback.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.ifsp.edu.feedback.dto.feedback.FeedbackRequestDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackResponseDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackSectorStatsDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackStatsDTO;
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

	//MONTA FEEDBACK E RETORNA DTO
	public FeedbackResponseDTO createFeedback(FeedbackRequestDTO dto, User author) {
	    Feedback feedback = Feedback.builder()
	            .titulo(dto.getTitulo()) // <- NOVO CAMPO
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
	            .titulo(feedback.getTitulo()) // <- NOVO CAMPO
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

	//MOSTRA CONTAGEM DE FEEDBACKS POR SETOR
	public List<FeedbackSectorStatsDTO> getTopSectorsWithMostFeedbacks(int limit) {
	    List<Feedback> feedbacks = feedbackRepository.findAll();

	    return feedbacks.stream()
	        .collect(Collectors.groupingBy(Feedback::getSector, Collectors.counting()))
	        .entrySet()
	        .stream()
	        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
	        .limit(limit)
	        .map(entry -> new FeedbackSectorStatsDTO(entry.getKey(), entry.getValue()))
	        .toList();
	}

	//ESTATISTICAS DOS ULTIMOS 3 MESES
	public FeedbackStatsDTO getLastThreeMonthsStats() {
	    LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
	    List<Feedback> recentFeedbacks = feedbackRepository.findByCreatedAtBetween(threeMonthsAgo, LocalDateTime.now());

	    long total = recentFeedbacks.size();

	    Map<String, Long> porSetor = recentFeedbacks.stream()
	            .collect(Collectors.groupingBy(Feedback::getSector, Collectors.counting()));

	    String setorMais = porSetor.entrySet().stream()
	            .max(Map.Entry.comparingByValue())
	            .map(Map.Entry::getKey).orElse(null);

	    long quantidadeSetorMais = porSetor.getOrDefault(setorMais, 0L);

	    Map<FeedbackType, Long> porTipo = recentFeedbacks.stream()
	            .collect(Collectors.groupingBy(Feedback::getType, Collectors.counting()));

	    FeedbackType tipoMais = porTipo.entrySet().stream()
	            .max(Map.Entry.comparingByValue())
	            .map(Map.Entry::getKey).orElse(null);

	    long quantidadeTipoMais = porTipo.getOrDefault(tipoMais, 0L);

	    return new FeedbackStatsDTO(
	            total,
	            setorMais,
	            quantidadeSetorMais,
	            tipoMais,
	            quantidadeTipoMais
	    );
	}

	//EXPORTA FEEDBACKS PARA .CSV
	public ByteArrayInputStream exportFeedbacksToCSV() {
	    List<Feedback> feedbacks = feedbackRepository.findAll();

	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    try (PrintWriter writer = new PrintWriter(out)) {
	        // Cabeçalho do CSV
	        writer.println("ID,Título,Conteúdo,Setor,Tipo,Anônimo,Data de Criação,Tags,Autor");

	        // Linhas com dados
	        for (Feedback feedback : feedbacks) {
	            writer.printf(
	                "%d,\"%s\",\"%s\",\"%s\",\"%s\",%b,\"%s\",\"%s\",\"%s\"%n",
	                feedback.getId(),
	                feedback.getTitulo().replace("\"", "\"\""), // <- NOVO CAMPO
	                feedback.getContent().replace("\"", "\"\""),
	                feedback.getSector(),
	                feedback.getType(),
	                feedback.isAnonymous(),
	                feedback.getCreatedAt(),
	                String.join(";", feedback.getTags()),
	                feedback.isAnonymous() ? "Anônimo" : feedback.getAuthor().getUsername()
	            );
	        }

	        writer.flush();
	        return new ByteArrayInputStream(out.toByteArray());

	    } catch (Exception e) {
	        throw new RuntimeException("Erro ao exportar CSV", e);
	    }
	}
}