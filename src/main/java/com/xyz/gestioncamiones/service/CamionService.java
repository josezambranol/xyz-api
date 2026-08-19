package com.xyz.gestioncamiones.service;

import com.xyz.gestioncamiones.dto.CamionRequest;
import com.xyz.gestioncamiones.dto.CamionResponse;
import com.xyz.gestioncamiones.entity.Camion;
import com.xyz.gestioncamiones.entity.Conductor;
import com.xyz.gestioncamiones.exception.ConflictoException;
import com.xyz.gestioncamiones.exception.RecursoNoEncontradoException;
import com.xyz.gestioncamiones.repository.CamionRepository;
import com.xyz.gestioncamiones.repository.ConductorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CamionService {
    private final CamionRepository camionRepository;
    private final ConductorRepository conductorRepository;

    public CamionService(CamionRepository camionRepository, ConductorRepository conductorRepository) {
        this.camionRepository = camionRepository;
        this.conductorRepository = conductorRepository;
    }

    @Transactional
    public CamionResponse crear(CamionRequest request) {
        String placa = request.placa().trim().toUpperCase();
        if (camionRepository.existsByPlacaIgnoreCase(placa)) {
            throw new ConflictoException("Ya existe un camión con la placa " + placa);
        }
        Camion camion = new Camion(placa, request.tipoVehiculo().trim());
        return CamionResponse.from(camionRepository.save(camion));
    }

    @Transactional(readOnly = true)
    public List<CamionResponse> listar() {
        return camionRepository.findAll().stream().map(CamionResponse::from).toList();
    }

    @Transactional
    public CamionResponse asociarConductor(Long camionId, Long conductorId) {
        Camion camion = camionRepository.findById(camionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el camión con id " + camionId));
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el conductor con id " + conductorId));
        camion.setConductor(conductor);
        return CamionResponse.from(camionRepository.save(camion));
    }
}
