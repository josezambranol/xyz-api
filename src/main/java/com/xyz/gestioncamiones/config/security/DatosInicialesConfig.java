package com.xyz.gestioncamiones.config.security;

import com.xyz.gestioncamiones.entity.Rol;
import com.xyz.gestioncamiones.entity.Usuario;
import com.xyz.gestioncamiones.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatosInicialesConfig {
    @Bean
    CommandLineRunner crearUsuariosIniciales(UsuarioRepository repository, PasswordEncoder encoder) {
        return args -> {
            if (!repository.existsByUsername("admin")) {
                repository.save(new Usuario("admin", encoder.encode("Admin123!"), Rol.ADMIN));
            }
            if (!repository.existsByUsername("supervisor")) {
                repository.save(new Usuario("supervisor", encoder.encode("Supervisor123!"), Rol.SUPERVISOR));
            }
        };
    }
}
