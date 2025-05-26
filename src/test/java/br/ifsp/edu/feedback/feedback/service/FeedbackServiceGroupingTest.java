package br.ifsp.edu.feedback.feedback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

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
public class FeedbackServiceGroupingTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private Feedback elogio, sugestao, critica;

    @BeforeEach
    void setup() {
        User user = User.builder().id(1L).username("alex").build();

        elogio = Feedback.builder()
                .id(1L)
                .titulo("Bom trabalho")
                .type(FeedbackType.ELOGIO)
                .anonymous(false)
                .author(user)
                .build();

        sugestao = Feedback.builder()
                .id(2L)
                .titulo("Melhoria no sistema")
                .type(FeedbackType.SUGESTAO)
                .anonymous(false)
                .author(user)
                .build();

        critica = Feedback.builder()
                .id(3L)
                .titulo("Problemas no processo")
                .type(FeedbackType.CRITICA)
                .anonymous(true)
                .author(user)
                .build();
    }

    @Test
    void shouldGroupFeedbacksByTypeCorrectly() {
        when(feedbackRepository.findAll()).thenReturn(List.of(elogio, sugestao, critica));

        Map<FeedbackType, List<FeedbackResponseDTO>> grouped = feedbackService.getFeedbacksGroupedByType();

        assertEquals(3, grouped.size());
        assertEquals(1, grouped.get(FeedbackType.ELOGIO).size());
        assertEquals(1, grouped.get(FeedbackType.SUGESTAO).size());
        assertEquals(1, grouped.get(FeedbackType.CRITICA).size());
    }

    @Test
    void shouldReturnEmptyMapWhenNoFeedbacksExist() {
        when(feedbackRepository.findAll()).thenReturn(List.of());

        Map<FeedbackType, List<FeedbackResponseDTO>> grouped = feedbackService.getFeedbacksGroupedByType();

        assertEquals(0, grouped.size());
    }
}
