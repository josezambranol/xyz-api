package com.xyz.gestioncamiones.dto;

import com.xyz.gestioncamiones.entity.Camion;

public record CamionResponse(Long id, String placa, String tipoVehiculo, ConductorResponse conductor) {
    public static CamionResponse from(Camion camion) {
        var conductor = camion.getConductor() == null ? null : ConductorResponse.from(camion.getConductor());
        return new CamionResponse(camion.getId(), camion.getPlaca(), camion.getTipoVehiculo(), conductor);
    }
}
