// src/config/database.js
import sqlite3 from 'sqlite3';
import { open } from 'sqlite';
import { EstadoPropiedad, Rol } from '../constants/enums.js';

let conexion = null;

// Función para conectar a SQLite y crear todas las tablas del sistema
export const conectarDB = async () => {
  if (conexion) {
    return conexion;
  }

  try {
    conexion = await open({
      filename: './inmobiliaria.sqlite',
      driver: sqlite3.Database
    });

    // Activar integridad referencial (claves foráneas) en SQLite
    await conexion.exec('PRAGMA foreign_keys = ON;');

    // 1. Tabla de Usuarios
    await conexion.exec(`
      CREATE TABLE IF NOT EXISTS Usuarios (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nombres TEXT NOT NULL,
        usuario TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        rol TEXT NOT NULL
      )
    `);

    // 2. Tabla de Propiedades
    await conexion.exec(`
      CREATE TABLE IF NOT EXISTS Propiedades (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        codigoInterno TEXT UNIQUE NOT NULL,
        direccion TEXT NOT NULL,
        tipo TEXT NOT NULL,
        modalidad TEXT NOT NULL,
        estado TEXT NOT NULL
      )
    `);

    // 3. Tabla de Clientes
    await conexion.exec(`
      CREATE TABLE IF NOT EXISTS Clientes (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        identificacion TEXT NOT NULL,
        nombre TEXT NOT NULL,
        apellido TEXT,
        telefono TEXT NOT NULL,
        correo TEXT
      )
    `);

    // Asegurar columna 'correo' si la tabla ya existía sin ella
    const columnasClientes = await conexion.all('PRAGMA table_info(Clientes)');
    const tieneCorreo = columnasClientes.some((col) => col.name === 'correo');
    if (!tieneCorreo) {
      await conexion.exec('ALTER TABLE Clientes ADD COLUMN correo TEXT');
    }

    // Índice único para impedir clientes duplicados con la misma identificación
    await conexion.exec('CREATE UNIQUE INDEX IF NOT EXISTS idx_clientes_identificacion ON Clientes(identificacion)');

    // 4. Tabla de Visitas (Relacionada con Clientes, Propiedades y Usuarios)
    await conexion.exec(`
      CREATE TABLE IF NOT EXISTS Visitas (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        idCliente INTEGER NOT NULL,
        idPropiedad INTEGER NOT NULL,
        idUsuario INTEGER NOT NULL,
        fecha TEXT NOT NULL,
        horaInicio TEXT NOT NULL,
        horaFin TEXT NOT NULL,
        estado TEXT NOT NULL,
        FOREIGN KEY (idCliente) REFERENCES Clientes(id),
        FOREIGN KEY (idPropiedad) REFERENCES Propiedades(id),
        FOREIGN KEY (idUsuario) REFERENCES Usuarios(id)
      )
    `);

    // 5. Tabla de Historial / Auditoría (RNF-03 y HU-09 - Relacionada con Visitas y Usuarios)
    await conexion.exec(`
      CREATE TABLE IF NOT EXISTS HistorialVisitas (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        idVisita INTEGER NOT NULL,
        idUsuario INTEGER NOT NULL,
        tipoCambio TEXT NOT NULL,
        motivo TEXT NOT NULL,
        fechaHoraCambio TEXT NOT NULL,
        fechaAnterior TEXT,
        fechaNueva TEXT,
        FOREIGN KEY (idVisita) REFERENCES Visitas(id),
        FOREIGN KEY (idUsuario) REFERENCES Usuarios(id)
      )
    `);

    // 6. Tabla de Avisos Internos (RN-10 - Relacionada con Usuarios)
    await conexion.exec(`
      CREATE TABLE IF NOT EXISTS Avisos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        idUsuario INTEGER NOT NULL,
        mensaje TEXT NOT NULL,
        fechaHora TEXT NOT NULL,
        FOREIGN KEY (idUsuario) REFERENCES Usuarios(id)
      )
    `);

    // Datos iniciales de prueba (solo si las tablas están vacías)
    await cargarDatosIniciales(conexion);

    return conexion;
  } catch (error) {
    console.error('Error al conectar con la base de datos:', error.message);
    throw error;
  }
};

// Inserta usuarios y propiedades iniciales para poder probar de inmediato
const cargarDatosIniciales = async (db) => {
  const totalUsuarios = await db.get('SELECT COUNT(*) as total FROM Usuarios');
  if (totalUsuarios.total === 0) {
    await db.run(
      'INSERT INTO Usuarios (id, nombres, usuario, password, rol) VALUES (?, ?, ?, ?, ?)',
      [1, 'Admin Principal', 'admin', 'admin123', Rol.ADMINISTRADOR]
    );
    await db.run(
      'INSERT INTO Usuarios (id, nombres, usuario, password, rol) VALUES (?, ?, ?, ?, ?)',
      [2, 'Carlos Asesor', 'carlos', 'carlos123', Rol.ASESOR]
    );
    await db.run(
      'INSERT INTO Usuarios (id, nombres, usuario, password, rol) VALUES (?, ?, ?, ?, ?)',
      [3, 'Laura Asesora', 'laura', 'laura123', Rol.ASESOR]
    );
  }

  const totalPropiedades = await db.get('SELECT COUNT(*) as total FROM Propiedades');
  if (totalPropiedades.total === 0) {
    await db.run(
      'INSERT INTO Propiedades (codigoInterno, direccion, tipo, modalidad, estado) VALUES (?, ?, ?, ?, ?)',
      ['P-01', 'Av. Amazonas', 'Casa', 'VENTA', EstadoPropiedad.DISPONIBLE]
    );
    await db.run(
      'INSERT INTO Propiedades (codigoInterno, direccion, tipo, modalidad, estado) VALUES (?, ?, ?, ?, ?)',
      ['P-02', 'Av. Shyris y Portugal', 'Departamento', 'ALQUILER', EstadoPropiedad.DISPONIBLE]
    );
  }
};

export default conectarDB;
