package com.inmobiliaria.constants;

import java.time.LocalTime;

/**
 * Horario laboral de atención de la inmobiliaria (RN-11).
 * Lunes a Viernes: 09:00 a 18:00 | Sábados: 09:00 a 13:00 | Domingos: Cerrado.
 */
public final class HorarioAtencion {
    public static final LocalTime APERTURA_LV = LocalTime.of(9, 0);
    public static final LocalTime CIERRE_LV = LocalTime.of(18, 0);
    public static final LocalTime APERTURA_SABADO = LocalTime.of(9, 0);
    public static final LocalTime CIERRE_SABADO = LocalTime.of(13, 0);

    private HorarioAtencion() {}
}
