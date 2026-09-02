package com.xyz.gestioncamiones.dto;

public record LoginResponse(
        String token,
        String tipo,
        String username,
        String rol
) {
    public LoginResponse(String token, String username, String rol) {
        this(token, "Bearer", username, rol);
    }
}
