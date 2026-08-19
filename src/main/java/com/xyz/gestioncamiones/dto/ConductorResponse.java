package com.xyz.gestioncamiones.dto;

import com.xyz.gestioncamiones.entity.Conductor;

public record ConductorResponse(Long id, String nombre) {
    public static ConductorResponse from(Conductor conductor) {
        return new ConductorResponse(conductor.getId(), conductor.getNombre());
    }
}
