package com.inmobiliaria.controller;

import com.inmobiliaria.model.Cliente;
import com.inmobiliaria.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Segunda Familia: Controller - ClientesController.
 * Depende de la interface ClienteService (Punto 18) y exige sesión iniciada (Punto 10 / RN-09).
 */
@RestController
@RequestMapping({"/api/clientes", "/clientes"})
public class ClientesController {

    private final ClienteService clienteService;

    public ClientesController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // POST /clientes (HU-04, RF-04)
    @PostMapping
    public ResponseEntity<Cliente> registrarCliente(@RequestBody Map<String, String> dto) {
        String nombre = dto.get("nombre");
        if ((nombre == null || nombre.isBlank()) && dto.get("nombres") != null) {
            nombre = dto.get("nombres");
        }

        Cliente cliente = new Cliente(
                null,
                dto.get("identificacion"),
                nombre,
                dto.get("apellido"),
                dto.get("telefono"),
                dto.get("correo")
        );

        Cliente creado = clienteService.registrarCliente(cliente);
        return ResponseEntity.status(201).body(creado);
    }

    // GET /clientes
    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    // GET /clientes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerClientePorId(id));
    }
}
