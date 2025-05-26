package br.ifsp.edu.feedback.feedback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.ifsp.edu.feedback.dto.feedback.FeedbackResponseDTO;
import br.ifsp.edu.feedback.model.Feedback;
import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import br.ifsp.edu.feedback.repository.FeedbackRepository;
import br.ifsp.edu.feedback.service.FeedbackService;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceAdminTest {

	@Mock 
	private FeedbackRepository feedbackRepository;
	
	@InjectMocks
	private FeedbackService feedbackService;
	
	private User user, user2;
	private Feedback praiseFeedback, praiseFeedback2, anonymousFeedback, anonymousFeedback2;
	 
	@BeforeEach
	void setup() {
		 user = User.builder()
                .id(1L)
                .username("employee1")
                .email("employee1@example.com")
                .password("password")
                .build();
		 
		 user2 = User.builder()
	                .id(2L)
	                .username("employee2")
	                .email("employee2@example.com")
	                .password("password")
	                .build();
				 
		 praiseFeedback = Feedback.builder()
	                .id(10L)
	                .titulo("Bom trabalho")
	                .content("Você fez um excelente trabalho!")
	                .sector("TI")
	                .type(FeedbackType.ELOGIO)
	                .anonymous(false)
	                .createdAt(LocalDateTime.of(2025, 5, 25, 0, 0))
	                .author(user2)
	                .build();
		 
		 anonymousFeedback = Feedback.builder()
			        .id(11L)
			        .titulo("Melhorar comunicação")
			        .content("Precisamos ser mais claros nas reuniões.")
			        .sector("Marketing")
			        .type(FeedbackType.CRITICA)
			        .anonymous(true)
			        .createdAt(LocalDateTime.of(2025, 5, 25, 0, 0))
			        .author(user)
			        .build();

	     praiseFeedback2 = Feedback.builder()
			        .id(12L)
			        .titulo("Excelente suporte")
			        .content("O suporte técnico resolveu meu problema rapidamente.")
			        .sector("Suporte")
			        .type(FeedbackType.ELOGIO)
			        .anonymous(false)
			        .createdAt(LocalDateTime.of(2025, 5, 24, 0, 0))
			        .author(user)
			        .build();

		 anonymousFeedback2 = Feedback.builder()
			        .id(13L)
			        .titulo("Sugestão para o sistema")
			        .content("Adicionar filtros avançados na busca.")
			        .sector("TI")
			        .type(FeedbackType.SUGESTAO)
			        .anonymous(true)
			        .createdAt(LocalDateTime.of(2025, 5, 23, 0, 0))
			        .author(user)
			        .build();

	}

	//getNonAnonymousFeedbacksByUser(User user)
	@Test
	public void shouldReturnOnlyNonAnonymousFeedbacksFromUser(){

		List<Feedback> feedbacks = List.of( anonymousFeedback, praiseFeedback2, anonymousFeedback2);
	    when(feedbackRepository.findByAuthor(user))
	    	.thenReturn(feedbacks);
	    List<FeedbackResponseDTO> result = feedbackService.getNonAnonymousFeedbacksByUser(user);

	    assertEquals(1, result.size());
	    assertTrue(result.stream().noneMatch(FeedbackResponseDTO::isAnonymous));
	    assertEquals("employee1", result.get(0).getAuthorName());
	    verify(feedbackRepository).findByAuthor(user);
	}
	
	@Test
	public void shouldReturnEmptyListWhenAllFeedbacksAreAnonymous(){
		List<Feedback> anonymousFeedbacks = List.of(anonymousFeedback, anonymousFeedback2);
	    when(feedbackRepository.findByAuthor(user))
	    	.thenReturn(anonymousFeedbacks);
	    List<FeedbackResponseDTO> result = feedbackService.getNonAnonymousFeedbacksByUser(user);

	    assertEquals(0, result.size());
	    assertTrue(result.stream().noneMatch(FeedbackResponseDTO::isAnonymous));
	    verify(feedbackRepository).findByAuthor(user);
	}
	
	//getAllFeedbacks() 
	@Test
	public void shouldReturnAllFeedbacksAsDTO() {
		List<Feedback> feedbacks = List.of( praiseFeedback, anonymousFeedback, praiseFeedback2, anonymousFeedback2);
	    when(feedbackRepository.findAll())
	    	.thenReturn(feedbacks);
	    List<FeedbackResponseDTO> result = feedbackService.getAllFeedbacks();

	    assertEquals(4, result.size());
	    assertEquals("employee2", result.get(0).getAuthorName());
	    assertEquals("employee1", result.get(2).getAuthorName());
	    verify(feedbackRepository).findAll();
	}
	
	//getFilteredFeedbacks(String setor, LocalDate data, LocalDate dataFinal)
	@Test
	void shouldReturnFeedbacksFilteredBySectorAndDateRange() {
	    String setor = "TI";
	    LocalDate dataInicial = LocalDate.of(2025, 5, 1);
	    LocalDate dataFinal = LocalDate.of(2025, 5, 31);

	    List<Feedback> mockResults = List.of(praiseFeedback, anonymousFeedback, praiseFeedback2);
	    when(feedbackRepository.findBySectorAndCreatedAtBetween("TI", dataInicial.atStartOfDay(), dataFinal.atTime(LocalTime.MAX)))
	    	.thenReturn(mockResults);
	    List<FeedbackResponseDTO> result = feedbackService.getFilteredFeedbacks(setor, dataInicial, dataFinal);

	    assertEquals(3, result.size());
	    assertEquals("Bom trabalho", result.get(0).getTitulo());
	    assertEquals("Melhorar comunicação", result.get(1).getTitulo());
	    assertEquals("Excelente suporte", result.get(2).getTitulo());
	    verify(feedbackRepository).findBySectorAndCreatedAtBetween(
	            "TI", dataInicial.atStartOfDay(), dataFinal.atTime(LocalTime.MAX)
	    );
	}
	
	@Test
	public void shouldFilterFeedbacksBySectorOnly() {
		String setor = "TI";
		LocalDate dataInicial = null;
		LocalDate dataFinal = null;

		List<Feedback> mockResults = List.of(praiseFeedback);
		when(feedbackRepository.findBySector("TI"))
			.thenReturn(mockResults);
		List<FeedbackResponseDTO> result = feedbackService.getFilteredFeedbacks(setor, dataInicial, dataFinal);

		assertEquals(1, result.size());
		assertEquals("Bom trabalho", result.get(0).getTitulo());
		verify(feedbackRepository).findBySector(
		     "TI"
		);
	}
	
	@Test
	public void shouldFilterBySectorAndSingleDate() {
		String setor = "TI";
		LocalDate dataInicial = LocalDate.of(2025, 5, 1);
		LocalDate dataFinal = null;

		List<Feedback> mockResults = List.of(praiseFeedback, anonymousFeedback2);
		when(feedbackRepository.findBySectorAndCreatedAtBetween("TI", dataInicial.atStartOfDay(), dataInicial.atTime(LocalTime.MAX)))
			.thenReturn(mockResults);
		List<FeedbackResponseDTO> result = feedbackService.getFilteredFeedbacks(setor, dataInicial, dataFinal);

		assertEquals(2, result.size());
		assertEquals("Bom trabalho", result.get(0).getTitulo());
		verify(feedbackRepository).findBySectorAndCreatedAtBetween(
		     "TI", dataInicial.atStartOfDay(), dataInicial.atTime(LocalTime.MAX)
		);
	}
	
	@Test
	public void shouldFilterBySingleDateOnly() {
		String setor = null;
		LocalDate dataInicial = LocalDate.of(2025, 5, 1);
		LocalDate dataFinal = null;

		List<Feedback> mockResults = List.of(praiseFeedback, anonymousFeedback2);
		when(feedbackRepository.findByCreatedAtBetween(dataInicial.atStartOfDay(), dataInicial.atTime(LocalTime.MAX)))
			.thenReturn(mockResults);
		List<FeedbackResponseDTO> result = feedbackService.getFilteredFeedbacks(setor, dataInicial, dataFinal);

		assertEquals(2, result.size());
		assertEquals("Bom trabalho", result.get(0).getTitulo());
		verify(feedbackRepository).findByCreatedAtBetween(
		     dataInicial.atStartOfDay(), dataInicial.atTime(LocalTime.MAX)
		);
	}
	
	@Test
	public void shouldReturnAllFeedbacksWhenNoFiltersProvided() {
		 String setor = null;
		 LocalDate dataInicial = null;
		 LocalDate dataFinal = null;

		 List<Feedback> mockResults = List.of(praiseFeedback, anonymousFeedback, praiseFeedback2, anonymousFeedback2);
		 when(feedbackRepository.findAll())
		    .thenReturn(mockResults);
		 List<FeedbackResponseDTO> result = feedbackService.getFilteredFeedbacks(setor, dataInicial, dataFinal);

		 assertEquals(4, result.size());
		 assertEquals("Bom trabalho", result.get(0).getTitulo());
		 assertEquals("Melhorar comunicação", result.get(1).getTitulo());
		 assertEquals("Excelente suporte", result.get(2).getTitulo());
		 verify(feedbackRepository).findAll();
	}
}
