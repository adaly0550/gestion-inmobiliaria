package com.inmobiliaria.exception;

/**
 * Excepción controlada del dominio (Punto 17).
 * Permite retornar el código HTTP exacto (400, 401, 403, 404) con formato { error, mensaje }.
 */
public class AppException extends RuntimeException {

    private final int status;

    public AppException(String mensaje, int status) {
        super(mensaje);
        this.status = status;
    }

    public AppException(String mensaje) {
        this(mensaje, 400);
    }

    public int getStatus() {
        return status;
    }
}
