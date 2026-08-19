package com.xyz.gestioncamiones.repository;

import com.xyz.gestioncamiones.entity.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConductorRepository extends JpaRepository<Conductor, Long> {}
