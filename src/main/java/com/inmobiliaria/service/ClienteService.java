package com.inmobiliaria.service;

import com.inmobiliaria.model.Cliente;

import java.util.List;

/**
 * Tercera Familia: Service (Interface) - ClienteService.
 * Define el contrato del módulo Clientes (HU-04, RF-04, RN-05).
 */
public interface ClienteService {

    Cliente registrarCliente(Cliente cliente);

    List<Cliente> listarClientes();

    Cliente obtenerClientePorId(Long id);
}
