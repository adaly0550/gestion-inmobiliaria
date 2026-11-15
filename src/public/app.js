// Apuntamos a los elementos del HTML
const formulario = document.getElementById('formulario-cliente');
const cuerpoTabla = document.getElementById('cuerpo-tabla');

// 1. Función para LEER (GET) los clientes y dibujarlos en la tabla
const cargarClientes = async () => {
    try {
        const respuesta = await fetch('/api/clientes');
        const clientes = await respuesta.json();

        // Limpiamos la tabla antes de rellenarla
        cuerpoTabla.innerHTML = '';

        if (!clientes || clientes.length === 0) {
            cuerpoTabla.innerHTML = '<tr><td colspan="5" style="text-align: center; color: #888;">No hay clientes registrados todavía.</td></tr>';
            return;
        }

        // Recorremos la lista y creamos una fila (tr) por cada cliente
        clientes.forEach(cliente => {
            const fila = document.createElement('tr');
            fila.innerHTML = `
                <td>${cliente.id}</td>
                <td>${cliente.identificacion}</td>
                <td>${cliente.nombre}</td>
                <td>${cliente.apellido}</td>
                <td>${cliente.telefono}</td>
            `;
            cuerpoTabla.appendChild(fila);
        });
    } catch (error) {
        console.error('Error al cargar los clientes:', error);
    }
};

// 2. Función para GUARDAR (POST) un nuevo cliente al enviar el formulario
formulario.addEventListener('submit', async (evento) => {
    evento.preventDefault();

    // Recolectamos los datos de los inputs
    const nuevoCliente = {
        identificacion: document.getElementById('identificacion').value.trim(),
        nombre: document.getElementById('nombre').value.trim(),
        apellido: document.getElementById('apellido').value.trim(),
        telefono: document.getElementById('telefono').value.trim()
    };

    try {
        const respuesta = await fetch('/api/clientes', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(nuevoCliente)
        });

        if (!respuesta.ok) {
            const error = await respuesta.json();
            alert('Error: ' + (error.error || error.mensaje || 'No se pudo guardar el cliente'));
            return;
        }

        // Limpiamos los campos del formulario
        formulario.reset();

        // Recargamos la tabla para ver el nuevo cliente inmediatamente
        await cargarClientes();
    } catch (error) {
        console.error('Error al guardar el cliente:', error);
        alert('Error de conexión con el servidor.');
    }
});

// Cuando la página se abra, cargamos la lista inmediatamente
cargarClientes();
