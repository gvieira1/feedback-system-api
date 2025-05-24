package br.ifsp.edu.feedback.report;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import br.ifsp.edu.feedback.controller.ReportController;
import br.ifsp.edu.feedback.dto.feedback.DashboardStatsDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackSectorStatsDTO;
import br.ifsp.edu.feedback.dto.feedback.FeedbackStatsDTO;
import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import br.ifsp.edu.feedback.service.ReportService;

@WebMvcTest(controllers = ReportController.class)
public class ReportControllerGetTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
	@MockBean
    private ReportService reportService;

    @Test
    public void shouldReturnTopSectorsWithMostFeedbacksWhenAdminAuthenticated() throws Exception {
        List<FeedbackSectorStatsDTO> mockSectors = List.of(
            new FeedbackSectorStatsDTO("TI", 20L),
            new FeedbackSectorStatsDTO("RH", 15L),
            new FeedbackSectorStatsDTO("Financeiro", 10L)
        );

        when(reportService.getTopSectorsWithMostFeedbacks(3)).thenReturn(mockSectors);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/top-sectors?limit=3")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].sector").value("TI"))
            .andExpect(jsonPath("$[0].count").value(20))
            .andExpect(jsonPath("$[1].sector").value("RH"))
            .andExpect(jsonPath("$[1].count").value(15))
            .andExpect(jsonPath("$[2].sector").value("Financeiro"))
            .andExpect(jsonPath("$[2].count").value(10));
    }

    @Test
    public void shouldUseDefaultLimitWhenNoLimitProvided() throws Exception {
        List<FeedbackSectorStatsDTO> mockSectors = List.of(
            new FeedbackSectorStatsDTO("TI", 20L),
            new FeedbackSectorStatsDTO("RH", 15L),
            new FeedbackSectorStatsDTO("Financeiro", 10L)
        );

        when(reportService.getTopSectorsWithMostFeedbacks(3)).thenReturn(mockSectors);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/top-sectors")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].sector").value("TI"))
            .andExpect(jsonPath("$[0].count").value(20))
            .andExpect(jsonPath("$[1].sector").value("RH"))
            .andExpect(jsonPath("$[1].count").value(15))
            .andExpect(jsonPath("$[2].sector").value("Financeiro"))
            .andExpect(jsonPath("$[2].count").value(10));
    }

    @Test
    public void shouldReturnUnauthorizedWhenNoAuthentication() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/top-sectors?limit=3"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    public void shouldReturnLastThreeMonthsStatsWhenAdminAuthenticated() throws Exception {
        FeedbackStatsDTO mockStats = new FeedbackStatsDTO(
            16,           // totalFeedbacks
            "TI",         // topSector
            8,            // topSectorCount
            FeedbackType.ELOGIO, // topType
            10            // topTypeCount
        );

        when(reportService.getLastThreeMonthsStats()).thenReturn(mockStats);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/last-three-months")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalFeedbacks").value(16))
            .andExpect(jsonPath("$.topSector").value("TI"))
            .andExpect(jsonPath("$.topSectorCount").value(8))
            .andExpect(jsonPath("$.mostFrequentType").value("ELOGIO"))
            .andExpect(jsonPath("$.mostFrequentTypeCount").value(10));
    }
    
    @Test
    public void shouldReturnDashboardStatsWhenAdminAuthenticated() throws Exception {
        Map<String, Long> feedbacksBySector = Map.of(
            "TI", 25L,
            "RH", 10L
        );
        Map<String, Long> feedbacksByType = Map.of(
            "ELOGIO", 15L,
            "RECLAMACAO", 20L
        );
        long anonymousCount = 5L;
        long nonAnonymousCount = 30L;

        DashboardStatsDTO mockDashboard = new DashboardStatsDTO(
            feedbacksBySector,
            feedbacksByType,
            anonymousCount,
            nonAnonymousCount
        );

        when(reportService.getDashboardStats()).thenReturn(mockDashboard);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/dashboard")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // Validações do Map feedbacksBySector
            .andExpect(jsonPath("$.feedbacksBySector.TI").value(25))
            .andExpect(jsonPath("$.feedbacksBySector.RH").value(10))
            // Validações do Map feedbacksByType
            .andExpect(jsonPath("$.feedbacksByType.ELOGIO").value(15))
            .andExpect(jsonPath("$.feedbacksByType.RECLAMACAO").value(20))
            // Validações das contagens
            .andExpect(jsonPath("$.anonymousCount").value(5))
            .andExpect(jsonPath("$.nonAnonymousCount").value(30));
    }
}