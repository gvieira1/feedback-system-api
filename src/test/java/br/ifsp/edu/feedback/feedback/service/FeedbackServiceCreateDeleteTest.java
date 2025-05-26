package br.ifsp.edu.feedback.feedback.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;

import br.ifsp.edu.feedback.dto.feedback.FeedbackRequestDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackResponseDTO;
import br.ifsp.edu.feedback.exception.ResourceNotFoundException;
import br.ifsp.edu.feedback.model.Feedback;
import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import br.ifsp.edu.feedback.repository.FeedbackRepository;
import br.ifsp.edu.feedback.service.FeedbackService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FeedbackServiceCreateDeleteTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private User author;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        author = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .build();
    }

    @Test
    void shouldCreateFeedbackWithCorrectData() {
        FeedbackRequestDTO request = FeedbackRequestDTO.builder()
                .titulo("Bom atendimento")
                .content("Fui muito bem atendido.")
                .sector("Recepção")
                .type(FeedbackType.ELOGIO)
                .anonymous(false)
                .tags(List.of("atendimento", "elogio"))
                .build();

        Feedback savedFeedback = Feedback.builder()
                .id(1L)
                .titulo(request.getTitulo())
                .content(request.getContent())
                .sector(request.getSector())
                .type(request.getType())
                .anonymous(request.isAnonymous())
                .tags(request.getTags())
                .author(author)
                .build();

        when(feedbackRepository.save(any(Feedback.class))).thenReturn(savedFeedback);

        FeedbackResponseDTO response = feedbackService.createFeedback(request, author);

        assertNotNull(response);
        assertEquals("Bom atendimento", response.getTitulo());
        assertEquals("johndoe", response.getAuthorName());

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(captor.capture());
        Feedback captured = captor.getValue();

        assertEquals("Recepção", captured.getSector());
        assertEquals(FeedbackType.ELOGIO, captured.getType());
    }

    @Test
    void shouldDeleteFeedbackWhenIdExists() {
        Feedback feedback = Feedback.builder().id(1L).build();

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

        feedbackService.deleteFeedback(1L);

        verify(feedbackRepository, times(1)).delete(feedback);
    }

    @Test
    void shouldThrowExceptionWhenFeedbackIdDoesNotExist() {
        when(feedbackRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> feedbackService.deleteFeedback(99L));
    }

    @Test
    void shouldThrowExceptionWhenCreateFeedbackWithInvalidData() {
        //comportamento inesperado do repositório (ex: falha de persistência)
        FeedbackRequestDTO request = FeedbackRequestDTO.builder()
                .titulo(null)
                .content(null)
                .sector("RH")
                .type(null)
                .anonymous(true)
                .build();

        when(feedbackRepository.save(any())).thenThrow(new IllegalArgumentException("Dados inválidos"));

        assertThrows(IllegalArgumentException.class, () -> feedbackService.createFeedback(request, author));
    }
}
