package br.ifsp.edu.feedback.feedback.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import br.ifsp.edu.feedback.controller.FeedbackController;
import br.ifsp.edu.feedback.dto.feedback.FeedbackRequestDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackResponseDTO;
import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.model.UserAuthenticated;
import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import br.ifsp.edu.feedback.service.ExportService;
import br.ifsp.edu.feedback.service.FeedbackService;

@WebMvcTest(controllers = FeedbackController.class)
public class FeedbackControllerPostTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
	@MockBean
    private FeedbackService feedbackService;

    @SuppressWarnings("removal")
	@MockBean
    private ExportService exportService;

    @Autowired
    private ObjectMapper objectMapper;

    private User getMockUser() {
        return User.builder()
                .id(1L)
                .username("employee1")
                .email("employee1@example.com")
                .password("password")
                .build();
    }

    @Test
    public void shouldCreateFeedbackSuccessfullyWhenValidRequestAndAuthenticated() throws Exception {
        User user = getMockUser();
        UserAuthenticated userAuth = new UserAuthenticated(user);

        FeedbackRequestDTO requestDto = FeedbackRequestDTO.builder()
                .titulo("Sugestão de melhoria")
                .content("Podemos melhorar o processo X")
                .sector("TI")
                .type(FeedbackType.SUGESTAO)
                .anonymous(false)
                .build();

        FeedbackResponseDTO responseDto = FeedbackResponseDTO.builder()
                .id(20L)
                .titulo(requestDto.getTitulo())
                .content(requestDto.getContent())
                .sector(requestDto.getSector())
                .type(requestDto.getType())
                .anonymous(requestDto.isAnonymous())
                .createdAt(LocalDateTime.now())
                .authorName(user.getUsername())
                .build();

        when(feedbackService.createFeedback(any(FeedbackRequestDTO.class), any(User.class)))
            .thenReturn(responseDto);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userAuth, null, userAuth.getAuthorities())
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/feedback")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(20))
            .andExpect(jsonPath("$.titulo").value("Sugestão de melhoria"))
            .andExpect(jsonPath("$.anonymous").value(false));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    public void shouldReturnBadRequestWhenFeedbackRequestIsInvalid() throws Exception {
        FeedbackRequestDTO invalidRequest = new FeedbackRequestDTO(); // tudo nulo

        mockMvc.perform(MockMvcRequestBuilders.post("/api/feedback")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnUnauthorizedWhenNoAuthentication() throws Exception {
        FeedbackRequestDTO requestDto = FeedbackRequestDTO.builder()
                .titulo("Teste")
                .content("Conteúdo")
                .sector("TI")
                .type(FeedbackType.ELOGIO)
                .anonymous(false)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/feedback")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isUnauthorized());
    }
}