package com.xyz.gestioncamiones.repository;

import com.xyz.gestioncamiones.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CamionRepository extends JpaRepository<Camion, Long> {
    boolean existsByPlacaIgnoreCase(String placa);
}
