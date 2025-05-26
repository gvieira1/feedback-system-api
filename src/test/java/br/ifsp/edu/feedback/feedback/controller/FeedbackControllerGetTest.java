package br.ifsp.edu.feedback.feedback.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import br.ifsp.edu.feedback.controller.FeedbackController;
import br.ifsp.edu.feedback.dto.feedback.FeedbackResponseDTO;
import br.ifsp.edu.feedback.exception.ResourceNotFoundException;
import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.model.UserAuthenticated;
import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import br.ifsp.edu.feedback.service.ExportService;
import br.ifsp.edu.feedback.service.FeedbackService;

@WebMvcTest(controllers = FeedbackController.class)
public class FeedbackControllerGetTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
	@MockBean
    private FeedbackService feedbackService;

    @SuppressWarnings("removal")
	@MockBean
    private ExportService exportService;

    private User getMockUser() {
        return User.builder()
                .id(1L)
                .username("employee1")
                .email("employee1@example.com")
                .password("password")
                .build();
    }
    
    private void authenticateAsUser(User user) {
        UserAuthenticated userAuth = new UserAuthenticated(user) {
			private static final long serialVersionUID = 1L;

			@Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
            }
        };

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userAuth, null, userAuth.getAuthorities())
        );
    }


    private void authenticateAsAdmin(User user) {
        UserAuthenticated adminAuth = new UserAuthenticated(user) {
			private static final long serialVersionUID = 1L;

			@Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        };
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(adminAuth, null, adminAuth.getAuthorities())
        );
    }


    @Test
    public void shouldReturnNonAnonymousFeedbacksForAuthenticatedEmployee() throws Exception {
        User user = getMockUser();
        authenticateAsUser(user);

        FeedbackResponseDTO feedback = FeedbackResponseDTO.builder()
                .id(10L)
                .titulo("Bom trabalho")
                .content("Você fez um excelente trabalho!")
                .sector("TI")
                .type(FeedbackType.ELOGIO)
                .anonymous(false)
                .createdAt(LocalDateTime.now())
                .authorName("João Silva")
                .build();

        when(feedbackService.getNonAnonymousFeedbacksByUser(any(User.class)))
            .thenReturn(List.of(feedback));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/feedback/me"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(10))
            .andExpect(jsonPath("$[0].titulo").value("Bom trabalho"))
            .andExpect(jsonPath("$[0].anonymous").value(false));
    }
    
    //TESTES DE EXPORTS
    @Test
    public void shouldExportFeedbacksToCsvSuccessfullyWhenAdminAuthenticated() throws Exception {
        Path mockPath = Path.of("exports/feedbacks.csv");
        when(exportService.exportFeedbacksToCsvFile()).thenReturn(mockPath);

        User user = getMockUser();
        authenticateAsAdmin(user);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/feedback/export"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Feedbacks exportados com sucesso para")));
    }

    @Test
    public void shouldReturnInternalServerErrorWhenExportFails() throws Exception {
        when(exportService.exportFeedbacksToCsvFile()).thenThrow(new RuntimeException("Falha inesperada"));

        User user = getMockUser();
        authenticateAsAdmin(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/feedback/export"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Erro ao exportar feedbacks")));
    }
    
    @Test
    public void shouldReturnAllFeedbacksWhenAdminAuthenticated() throws Exception {
    	User adminUser = getMockUser();
        authenticateAsAdmin(adminUser);

         List<FeedbackResponseDTO> feedbackResponse = List.of(
        		FeedbackResponseDTO.builder()
                 .id(9L)
                 .titulo("Ótima Organização")
                 .content("Um dos melhores eventos corporativos que já fui")
                 .sector("RH")
                 .type(FeedbackType.ELOGIO)
                 .anonymous(false)
                 .createdAt(LocalDateTime.now())
                 .authorName("Oscar Rodrigues")
                 .build()
         );
         when(feedbackService.getAllFeedbacks()).thenReturn(feedbackResponse);
         
         mockMvc.perform(MockMvcRequestBuilders.get("/api/feedback"))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$[0].id").value(9))
                 .andExpect(jsonPath("$[0].titulo").value("Ótima Organização"))
                 .andExpect(jsonPath("$[0].anonymous").value(false));
         
    }
    
    @Test
    public void shouldReturnFilteredFeedbacksBySectorAndDateWhenAdminAuthenticated() throws Exception {
    	User adminUser = getMockUser();
        authenticateAsAdmin(adminUser);
        
        List<FeedbackResponseDTO> feedbackResponse = List.of(     
                 FeedbackResponseDTO.builder()
                 .id(10L)
                 .titulo("Resolução Rápida")
                 .content("Problema resolvido em 10 minutos! Estão de parabéns.")
                 .sector("TI")
                 .type(FeedbackType.ELOGIO)
                 .anonymous(false)
                 .createdAt(LocalDateTime.of(2025, 05, 25, 0, 0))
                 .authorName("Sandro Gusmão")
                 .build()
         );
         when(feedbackService.getFilteredFeedbacks("TI", LocalDate.of(2025, 05, 25), null)).thenReturn(feedbackResponse);
         
        mockMvc.perform(
        	MockMvcRequestBuilders.get("/api/feedback/filter")
        		.param("setor", "TI")
            	.param("data", "2025-05-25"))
        .andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].sector").value("TI"))
			.andExpect(jsonPath("$[0].titulo").value("Resolução Rápida"));
    }
    
    @Test
    public void shouldReturnAllFeedbacksWhenNoFiltersProvided() throws Exception {
    	User adminUser = getMockUser();
        authenticateAsAdmin(adminUser);
        
        List<FeedbackResponseDTO> feedbackResponse = List.of(     
                 FeedbackResponseDTO.builder()
                 .id(10L)
                 .titulo("Resolução Rápida")
                 .content("Problema resolvido em 10 minutos! Estão de parabéns.")
                 .sector("TI")
                 .type(FeedbackType.ELOGIO)
                 .anonymous(false)
                 .createdAt(LocalDateTime.of(2025, 05, 25, 0, 0))
                 .authorName("Sandro Gusmão")
                 .build(),
                 
                 FeedbackResponseDTO.builder()
                 .id(1L)
                 .titulo("Ótimo serviço")
                 .content("Mandou bem no projeto!")
                 .sector("RH")
                 .type(FeedbackType.ELOGIO)
                 .anonymous(true)
                 .createdAt(LocalDateTime.of(2025, 5, 25, 10, 0))
                 .authorName("João")
                 .build()
         );
         when(feedbackService.getFilteredFeedbacks(null, null, null)).thenReturn(feedbackResponse);
         
         mockMvc.perform(
             	MockMvcRequestBuilders.get("/api/feedback/filter"))
     			.andExpect(status().isOk())
     			.andExpect(jsonPath("$[0].sector").value("TI"))
     			.andExpect(jsonPath("$[0].titulo").value("Resolução Rápida"))
     			.andExpect(jsonPath("$[1].sector").value("RH"))
     			.andExpect(jsonPath("$[1].titulo").value("Ótimo serviço"));
    }
    
    @Test
    public void shouldDeleteFeedbackByIdWhenAdminAuthenticated() throws Exception {
    	Long feedbackId = 1L;
    	User adminUser = getMockUser();
        authenticateAsAdmin(adminUser);
      
        doNothing().when(feedbackService).deleteFeedback(feedbackId);
        
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/feedback/{id}", feedbackId)
        	.with(csrf()))
            .andExpect(status().isNoContent());
    }
    
    @Test
    public void shouldReturnNotFoundWhenFeedbackIdDoesNotExist() throws Exception {

        Long idInvalido = 99L;
    	User adminUser = getMockUser();
        authenticateAsAdmin(adminUser);

        doThrow(new ResourceNotFoundException("Feedback não encontrado"))
            .when(feedbackService).deleteFeedback(idInvalido);

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/feedback/{id}", idInvalido)
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Feedback não encontrado")));
    }
    
    @Test
    public void shouldReturnForbiddenWhenUserIsNotAdmin() throws Exception {
        Long feedbackId = 199L;

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/feedback/{id}", feedbackId)
                .with(user("employee1").roles("EMPLOYEE")))  
            .andDo(print())
            .andExpect(status().isForbidden());
    }
    
    @Test
    public void shouldReturnGroupedFeedbacksByTypeWhenAdminAuthenticated() throws Exception {
    	User adminUser = getMockUser();
        authenticateAsAdmin(adminUser);
        
        Map<FeedbackType, List<FeedbackResponseDTO>> mockMap = new HashMap<>();

        List<FeedbackResponseDTO> elogios = List.of(
            FeedbackResponseDTO.builder()
                .id(1L)
                .titulo("Ótimo serviço")
                .content("Excelente atendimento")
                .sector("TI")
                .type(FeedbackType.ELOGIO)
                .anonymous(false)
                .createdAt(LocalDateTime.now())
                .authorName("Maria")
                .build()
        );

        List<FeedbackResponseDTO> criticas = List.of(
            FeedbackResponseDTO.builder()
                .id(2L)
                .titulo("Pode melhorar")
                .content("Mais atenção aos prazos")
                .sector("RH")
                .type(FeedbackType.CRITICA)
                .anonymous(true)
                .createdAt(LocalDateTime.now())
                .authorName("João")
                .build()
        );

        mockMap.put(FeedbackType.ELOGIO, elogios);
        mockMap.put(FeedbackType.CRITICA, criticas);

        when(feedbackService.getFeedbacksGroupedByType()).thenReturn(mockMap);
        
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/feedback/grouped-by-type"))
            .andExpect(status().isOk())
        	.andExpect(jsonPath("$.CRITICA[0].sector").value("RH"))
			.andExpect(jsonPath("$.ELOGIO[0].sector").value("TI"));

    }
}
