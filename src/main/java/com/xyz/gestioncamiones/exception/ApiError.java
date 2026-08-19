package com.xyz.gestioncamiones.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(Instant timestamp, int status, String error, String mensaje,
                       String path, Map<String, String> erroresValidacion) {}
