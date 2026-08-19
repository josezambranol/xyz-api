package com.xyz.gestioncamiones.controller;

import com.xyz.gestioncamiones.dto.ConductorRequest;
import com.xyz.gestioncamiones.dto.ConductorResponse;
import com.xyz.gestioncamiones.service.ConductorService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conductores")
public class ConductorController {
    private final ConductorService conductorService;

    public ConductorController(ConductorService conductorService) { this.conductorService = conductorService; }

    @PostMapping
    public ResponseEntity<ConductorResponse> crear(@Valid @RequestBody ConductorRequest request) {
        ConductorResponse creado = conductorService.crear(request);
        return ResponseEntity.created(URI.create("/api/conductores/" + creado.id())).body(creado);
    }

    @GetMapping
    public List<ConductorResponse> listar() { return conductorService.listar(); }
}
