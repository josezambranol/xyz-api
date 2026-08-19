package com.xyz.gestioncamiones.controller;

import com.xyz.gestioncamiones.dto.CamionRequest;
import com.xyz.gestioncamiones.dto.CamionResponse;
import com.xyz.gestioncamiones.service.CamionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {
    private final CamionService camionService;

    public CamionController(CamionService camionService) { this.camionService = camionService; }

    @PostMapping
    public ResponseEntity<CamionResponse> crear(@Valid @RequestBody CamionRequest request) {
        CamionResponse creado = camionService.crear(request);
        return ResponseEntity.created(URI.create("/api/camiones/" + creado.id())).body(creado);
    }

    @GetMapping
    public List<CamionResponse> listar() { return camionService.listar(); }

    @PutMapping("/{camionId}/conductor/{conductorId}")
    public CamionResponse asociar(@PathVariable Long camionId, @PathVariable Long conductorId) {
        return camionService.asociarConductor(camionId, conductorId);
    }
}
