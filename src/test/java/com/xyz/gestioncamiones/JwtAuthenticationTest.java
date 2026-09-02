package com.xyz.gestioncamiones;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.gestioncamiones.dto.CamionRequest;
import com.xyz.gestioncamiones.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String obtenerToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("token").asText();
    }

    @Test
    @DisplayName("Login exitoso con usuario admin debe retornar token y datos")
    void testLoginAdminExitoso() throws Exception {
        LoginRequest request = new LoginRequest("admin", "Admin123!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.rol").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Login exitoso con usuario supervisor debe retornar token y rol supervisor")
    void testLoginSupervisorExitoso() throws Exception {
        LoginRequest request = new LoginRequest("supervisor", "Supervisor123!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.username").value("supervisor"))
                .andExpect(jsonPath("$.rol").value("ROLE_SUPERVISOR"));
    }

    @Test
    @DisplayName("Login con credenciales inválidas debe retornar 401 Unauthorized")
    void testLoginCredencialesInvalidas() throws Exception {
        LoginRequest request = new LoginRequest("admin", "ClaveIncorrecta!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Petición a endpoint protegido sin token debe retornar 401 Unauthorized")
    void testEndpointProtegidoSinToken() throws Exception {
        mockMvc.perform(get("/api/camiones"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Petición a endpoint protegido con token inválido debe retornar 401 Unauthorized")
    void testEndpointProtegidoTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/camiones")
                        .header("Authorization", "Bearer token_falso_invalido_123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Admin puede listar camiones y crear camiones con su token JWT")
    void testAdminAccesoCompleto() throws Exception {
        String token = obtenerToken("admin", "Admin123!");
        assertNotNull(token);

        // GET permitido
        mockMvc.perform(get("/api/camiones")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // POST permitido para ADMIN
        CamionRequest camionRequest = new CamionRequest("JWT" + (System.currentTimeMillis() % 1000), "Tractomula");
        mockMvc.perform(post("/api/camiones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(camionRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Supervisor puede listar camiones pero NO puede crear camiones (403 Forbidden)")
    void testSupervisorPermisos() throws Exception {
        String token = obtenerToken("supervisor", "Supervisor123!");
        assertNotNull(token);

        // GET permitido para SUPERVISOR
        mockMvc.perform(get("/api/camiones")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // POST denegado para SUPERVISOR (requiere ADMIN)
        CamionRequest camionRequest = new CamionRequest("SUP" + (System.currentTimeMillis() % 1000), "Furgon");
        mockMvc.perform(post("/api/camiones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(camionRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
