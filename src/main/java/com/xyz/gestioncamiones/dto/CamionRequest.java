package com.xyz.gestioncamiones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CamionRequest(
        @NotBlank(message = "La placa es obligatoria")
        @Size(max = 10, message = "La placa puede tener máximo 10 caracteres") String placa,
        @NotBlank(message = "El tipo de vehículo es obligatorio")
        @Size(max = 60, message = "El tipo de vehículo puede tener máximo 60 caracteres") String tipoVehiculo) {}
