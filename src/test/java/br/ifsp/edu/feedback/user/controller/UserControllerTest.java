package br.ifsp.edu.feedback.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.ifsp.edu.feedback.FeedbackApplication;
import br.ifsp.edu.feedback.dto.user.UserDTO;
import br.ifsp.edu.feedback.model.User;
import br.ifsp.edu.feedback.model.enumerations.Role;
import br.ifsp.edu.feedback.repository.UserRepository;
import br.ifsp.edu.feedback.service.UserService;

@SpringBootTest(classes = FeedbackApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
	@MockBean
    private UserRepository userRepository;

    @SuppressWarnings("removal")
	@MockBean
    private UserService userService;

    @BeforeEach
    void setup() {
        Mockito.reset(userRepository, userService);

        Mockito.when(userRepository.save(Mockito.any(User.class)))
               .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testPublicRegister() throws Exception {
        String json = """
            {
                "username": "user1",
                "email": "user1@test.com",
                "password": "12345678",
                "passwordConfirmation": "12345678"
            }
            """;

        mockMvc.perform(post("/api/users/public-register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    @Test
    void testPublicRegisterWithInvalidData() throws Exception {
        String json = """
            {
                "username": "",
                "email": "not-an-email",
                "password": "123456",
                "passwordConfirmation": "different"
            }
            """;

        mockMvc.perform(post("/api/users/public-register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminRegister() throws Exception {
        String json = """
            {
                "username": "adminuser",
                "email": "admin@test.com",
                "password": "admin123",
                "passwordConfirmation": "admin123",
                "role": "ADMIN"
            }
            """;

        mockMvc.perform(post("/api/users/admin-register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testAdminRegisterForbiddenForNonAdmin() throws Exception {
        String json = """
            {
                "username": "user2",
                "email": "user2@test.com",
                "password": "123456",
                "passwordConfirmation": "123456",
                "role": "EMPLOYEE"
            }
            """;

        mockMvc.perform(post("/api/users/admin-register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isForbidden());
    }

    //TESTES PARA LISTAGEM DE USUÁRIOS
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsersReturnsUserList() throws Exception {
        List<UserDTO> mockUsers = Arrays.asList(
            new UserDTO(1L, "user1", "user1@test.com", Role.EMPLOYEE),
            new UserDTO(2L, "admin", "admin@test.com", Role.ADMIN)
        );

        Mockito.when(userService.getAllUsersDTO()).thenReturn(mockUsers);

        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].username").value("user1"))
            .andExpect(jsonPath("$[1].email").value("admin@test.com"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetAllUsersForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllUsersUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized());
    }
}
