package com.xyz.gestioncamiones;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.gestioncamiones.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSeguridadIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void todosLosEndpointsExigenAutenticacion() throws Exception {
        mockMvc.perform(get("/api/camiones")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/conductores")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/camiones").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC123\",\"tipoVehiculo\":\"Furgón\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/camiones/1/conductor/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void usuariosEstanEnH2YConPasswordBCrypt() {
        var admin = usuarioRepository.findByUsername("admin").orElseThrow();
        var supervisor = usuarioRepository.findByUsername("supervisor").orElseThrow();
        assertThat(admin.getPassword()).startsWith("$2");
        assertThat(supervisor.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("Admin123!", admin.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("Supervisor123!", supervisor.getPassword())).isTrue();
    }

    @Test
    void adminCreaSupervisorAsociaYAmbosConsultan() throws Exception {
        String sufijo = String.valueOf(System.nanoTime()).substring(8);
        String placa = ("T" + sufijo).substring(0, Math.min(7, sufijo.length() + 1));

        String camionJson = mockMvc.perform(post("/api/camiones")
                        .with(httpBasic("admin", "Admin123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"" + placa + "\",\"tipoVehiculo\":\"Tractocamión\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conductor").isEmpty())
                .andReturn().getResponse().getContentAsString();

        String conductorJson = mockMvc.perform(post("/api/conductores")
                        .with(httpBasic("admin", "Admin123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Laura Gómez\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode camion = objectMapper.readTree(camionJson);
        JsonNode conductor = objectMapper.readTree(conductorJson);

        mockMvc.perform(put("/api/camiones/{camionId}/conductor/{conductorId}",
                        camion.get("id").asLong(), conductor.get("id").asLong())
                        .with(httpBasic("supervisor", "Supervisor123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conductor.nombre").value("Laura Gómez"));

        mockMvc.perform(get("/api/camiones").with(httpBasic("admin", "Admin123!")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/conductores").with(httpBasic("supervisor", "Supervisor123!")))
                .andExpect(status().isOk());
    }

    @Test
    void rolesIncorrectosReciben403() throws Exception {
        mockMvc.perform(post("/api/camiones")
                        .with(httpBasic("supervisor", "Supervisor123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"XYZ789\",\"tipoVehiculo\":\"Camión\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/camiones/1/conductor/1")
                        .with(httpBasic("admin", "Admin123!")))
                .andExpect(status().isForbidden());
    }
}
