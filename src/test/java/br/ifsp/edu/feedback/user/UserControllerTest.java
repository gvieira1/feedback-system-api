package br.ifsp.edu.feedback.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = br.ifsp.edu.feedback.FeedbackApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPublicRegister() throws Exception {
        String json = """
            {
                "username": "user1",
                "email": "user1@test.com",
                "password": "123456",
                "passwordConfirmation": "123456"
            }
            """;

        mockMvc.perform(post("/api/users/public-register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    // Para testar o admin-register, precisa de autenticação JWT válida com role ADMIN
    // MockJwtConfigurer ou alguma lib para mockar autenticação no teste
    // exemplo básico de como poderia ser (sem implementar o token real):

//    @Test
//    void testAdminRegister() throws Exception {
//        String json = """
//            {
//                "username": "adminUser",
//                "email": "admin@test.com",
//                "password": "admin123",
//                "passwordConfirmation": "admin123",
//                "role": "ADMIN"
//            }
//            """;
//
//        mockMvc.perform(post("/api/users/admin-register")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json)
//                .header("Authorization", "Bearer <token-de-teste-com-ROLE_ADMIN>"))
//            .andExpect(status().isOk());
//    }
}
