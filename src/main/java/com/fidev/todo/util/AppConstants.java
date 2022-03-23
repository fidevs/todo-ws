package com.fidev.todo.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class AppConstants {

    AppConstants() {
        PARAMS.put("desc", "description");
        PARAMS.put("date", "finDate");
        PARAMS.put("duration", "duration");
        PARAMS.put("delay", "delay");
        PARAMS.put("status", "status");
    }

    public static final String PENDING_STATUS = "PENDING";
    public static final String COMPLETED_STATUS = "COMPLETED";
    public static final String DELETED_STATUS = "DELETED";

    public static final String INVALID_ACTION_CODE = "INVALID_ACTION";
    public static final String INVALID_ACTION_MESSAGE = "No es posible actualizar una tarea completada";
    public static final String INVALID_DURATION_CODE = "INVALID_DURATION";
    public static final String INVALID_DURATION_MESSAGE = "La duración estimada no puede ser menor a 1";
    public static final String INVALID_DESC_CODE = "INVALID_DESC";
    public static final String INVALID_DESC_MESSAGE = "La descripción de la tarea no es valida";
    public static final String NOT_FOUND_CODE = "NOT_FOUND";
    public static final String NOT_FOUND_MESSAGE = "No se encontró la tarea con el ID solicitado";

    public static final Map<String, String> PARAMS = new HashMap<>();
}
