package br.ifsp.edu.feedback.feedback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ifsp.edu.feedback.dto.feedback.FeedbackResponseDTO;
import br.ifsp.edu.feedback.model.Feedback;
import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import br.ifsp.edu.feedback.service.FeedbackService;

public class FeedbackServiceConversionTest {

    private FeedbackService feedbackService;
    private User author;

    @BeforeEach
    void setup() {
        feedbackService = new FeedbackService(null); // null porque não usamos o repositório neste teste
        author = User.builder().id(1L).username("alex").build();
    }

    @Test
    void shouldConvertFeedbackToDTOWithAuthorName() {
        Feedback feedback = Feedback.builder()
                .id(1L)
                .titulo("Parabéns")
                .content("Excelente resultado.")
                .sector("TI")
                .type(FeedbackType.ELOGIO)
                .anonymous(false)
                .createdAt(LocalDateTime.now())
                .tags(List.of("proatividade", "resultado"))
                .author(author)
                .build();

        FeedbackResponseDTO dto = feedbackService.toResponseDTO(feedback);

        assertEquals("alex", dto.getAuthorName());
        assertEquals("Parabéns", dto.getTitulo());
        assertEquals("TI", dto.getSector());
    }

    @Test
    void shouldConvertFeedbackToDTOWithoutAuthorNameWhenAnonymous() {
        Feedback feedback = Feedback.builder()
                .id(2L)
                .titulo("Sugestão")
                .content("Melhorar comunicação interna.")
                .sector("RH")
                .type(FeedbackType.SUGESTAO)
                .anonymous(true)
                .createdAt(LocalDateTime.now())
                .tags(List.of("comunicacao"))
                .author(author)
                .build();

        FeedbackResponseDTO dto = feedbackService.toResponseDTO(feedback);

        assertNull(dto.getAuthorName());
        assertEquals("Sugestão", dto.getTitulo());
    }
}
