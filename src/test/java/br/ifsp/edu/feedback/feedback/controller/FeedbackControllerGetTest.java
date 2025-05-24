package br.ifsp.edu.feedback.feedback.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.nio.file.Path;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import br.ifsp.edu.feedback.controller.FeedbackController;
import br.ifsp.edu.feedback.dto.feedback.FeedbackResponseDTO;
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

    @Test
    public void shouldReturnNonAnonymousFeedbacksForAuthenticatedEmployee() throws Exception {
        User user = getMockUser();
        UserAuthenticated userAuth = new UserAuthenticated(user);

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

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userAuth, null, userAuth.getAuthorities())
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/feedback/me"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(10))
            .andExpect(jsonPath("$[0].titulo").value("Bom trabalho"))
            .andExpect(jsonPath("$[0].anonymous").value(false));
    }
    
    //TESTES DE EXPORTS
    @SuppressWarnings("serial")
	@Test
    public void shouldExportFeedbacksToCsvSuccessfullyWhenAdminAuthenticated() throws Exception {
        Path mockPath = Path.of("exports/feedbacks.csv");
        when(exportService.exportFeedbacksToCsvFile()).thenReturn(mockPath);

        User user = User.builder()
            .id(2L)
            .username("admin")
            .email("admin@example.com")
            .password("password")
            .build();
        
        UserAuthenticated adminAuth = new UserAuthenticated(user) {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        };

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(adminAuth, null, adminAuth.getAuthorities())
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/feedback/export"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Feedbacks exportados com sucesso para")));
    }

    @Test
    public void shouldReturnInternalServerErrorWhenExportFails() throws Exception {
        when(exportService.exportFeedbacksToCsvFile()).thenThrow(new RuntimeException("Falha inesperada"));

        User adminUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .password("password")
                .build();

        @SuppressWarnings("serial")
		UserAuthenticated adminAuth = new UserAuthenticated(adminUser) {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        };

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminAuth, null, adminAuth.getAuthorities())
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/feedback/export"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Erro ao exportar feedbacks")));
    }
}
