package com.inmobiliaria.service.impl;

import com.inmobiliaria.exception.AppException;
import com.inmobiliaria.model.Cliente;
import com.inmobiliaria.repository.ClienteRepository;
import com.inmobiliaria.service.ClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cuarta Familia: Implementación - ClienteServiceImpl.
 * Cumple el contrato ClienteService e inyecta ClienteRepository por constructor (Punto 18).
 */
@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public Cliente registrarCliente(Cliente cliente) {
        validarDatosObligatorios(cliente);

        String identificacionLimpia = cliente.getIdentificacion().trim();
        if (clienteRepository.findByIdentificacion(identificacionLimpia).isPresent()) {
            throw new AppException("Ya existe un cliente registrado con la identificación " + identificacionLimpia + ".", 400);
        }

        cliente.setIdentificacion(identificacionLimpia);
        cliente.setNombre(cliente.getNombre().trim());
        cliente.setApellido(cliente.getApellido() != null ? cliente.getApellido().trim() : "");
        cliente.setTelefono(cliente.getTelefono().trim());
        cliente.setCorreo(cliente.getCorreo() != null && !cliente.getCorreo().isBlank() ? cliente.getCorreo().trim() : null);

        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {
        return clienteRepository.findAllByOrderByIdDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerClientePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new AppException("Cliente no encontrado.", 404));
    }

    // Método privado del diagrama de ClienteServiceImpl (RN-05)
    private void validarDatosObligatorios(Cliente cliente) {
        if (cliente == null
                || cliente.getIdentificacion() == null || cliente.getIdentificacion().isBlank()
                || cliente.getNombre() == null || cliente.getNombre().isBlank()
                || cliente.getTelefono() == null || cliente.getTelefono().isBlank()) {
            throw new AppException("RN-05: Identificación, nombre y teléfono son obligatorios.", 400);
        }
    }
}
