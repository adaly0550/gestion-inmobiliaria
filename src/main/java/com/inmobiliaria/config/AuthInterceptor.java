package com.inmobiliaria.config;

import com.inmobiliaria.exception.AppException;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor de autenticación y autorización (Puntos 8 y 10 / RNF-01, RNF-02, RN-09).
 * Lee el ID de usuario desde la sesión HTTP del servidor (cookie httpOnly) y carga
 * el Usuario real desde la base de datos en cada petición.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER_ID = "USUARIO_ID_SESION";
    public static final String REQUEST_USER_ATTR = "usuarioAutenticado";

    private final UsuarioRepository usuarioRepository;

    public AuthInterceptor(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();

        // Rutas públicas de autenticación
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/logout")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Long usuarioId = (session != null && session.getAttribute(SESSION_USER_ID) != null)
                ? (Long) session.getAttribute(SESSION_USER_ID)
                : null;

        // Sincronizar con la sesión activa de la pestaña si se envía x-usuario-id
        String headerId = request.getHeader("x-usuario-id");
        if (headerId != null && !headerId.isBlank()) {
            try {
                Long idPestana = Long.valueOf(headerId.trim());
                if (usuarioId == null || !usuarioId.equals(idPestana)) {
                    usuarioId = idPestana;
                    HttpSession sesionActiva = request.getSession(true);
                    sesionActiva.setAttribute(SESSION_USER_ID, usuarioId);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (usuarioId == null) {
            throw new AppException("No autorizado: Debe iniciar sesión para acceder al sistema.", 401);
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new AppException("Usuario no encontrado.", 404));

        request.setAttribute(REQUEST_USER_ATTR, usuario);
        return true;
    }
}
