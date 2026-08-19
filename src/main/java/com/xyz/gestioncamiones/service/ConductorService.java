package com.xyz.gestioncamiones.service;

import com.xyz.gestioncamiones.dto.ConductorRequest;
import com.xyz.gestioncamiones.dto.ConductorResponse;
import com.xyz.gestioncamiones.entity.Conductor;
import com.xyz.gestioncamiones.repository.ConductorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConductorService {
    private final ConductorRepository conductorRepository;

    public ConductorService(ConductorRepository conductorRepository) {
        this.conductorRepository = conductorRepository;
    }

    @Transactional
    public ConductorResponse crear(ConductorRequest request) {
        return ConductorResponse.from(conductorRepository.save(new Conductor(request.nombre().trim())));
    }

    @Transactional(readOnly = true)
    public List<ConductorResponse> listar() {
        return conductorRepository.findAll().stream().map(ConductorResponse::from).toList();
    }
}
