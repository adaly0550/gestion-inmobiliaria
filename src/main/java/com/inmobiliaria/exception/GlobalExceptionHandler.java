package com.inmobiliaria.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador centralizado de errores HTTP (Punto 17 y REGLAS.md §1.4).
 * Garantiza que toda respuesta de error tenga siempre { error, mensaje } y el código HTTP adecuado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, String>> handleAppException(AppException ex) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", ex.getMessage());
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadJson(HttpMessageNotReadableException ex) {
        String msg = "Formato de datos inválido en la petición.";
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", msg);
        body.put("mensaje", msg);
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor.";
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", msg);
        body.put("mensaje", msg);
        return ResponseEntity.status(500).body(body);
    }
}
