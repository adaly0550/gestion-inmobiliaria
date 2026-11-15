-- ============================================================================
-- PROYECTO FINAL DE IMPLEMENTACIÓN DE SOFTWARE - IST CENESTUR
-- Sistema de Gestión Inmobiliaria (Spring Boot 3 + Spring Data JPA / MySQL / H2)
-- Autores: Grecco Cedeño y Britany Tahis Macias
-- Archivo: sql/bd_gestion_inmobiliaria.sql
-- Propósito: Script de creación de tablas (DER) y carga inicial de 10 propiedades,
--            usuarios con contraseñas BCrypt, clientes y visitas de demostración.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS inmobiliaria_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE inmobiliaria_db;

-- 1. Tabla de Usuarios (Administradores y Asesores) - RNF-01, RNF-02, RN-09
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(120) NOT NULL,
    usuario VARCHAR(60) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL
);

-- 2. Tabla de Propiedades (Catálogo de Casas y Departamentos) - HU-01, HU-02, HU-03, RN-01, RN-02, RN-05
CREATE TABLE IF NOT EXISTS propiedades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_interno VARCHAR(30) NOT NULL UNIQUE,
    direccion VARCHAR(200) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    modalidad VARCHAR(30) NOT NULL,
    descripcion VARCHAR(500),
    estado VARCHAR(30) NOT NULL
);

-- 3. Tabla de Clientes - HU-04, RF-04, RN-05
CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identificacion VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),
    telefono VARCHAR(30) NOT NULL,
    correo VARCHAR(120)
);

-- 4. Tabla de Visitas (Duración fija de 60 minutos, nunca se eliminan físicamente) - HU-05 a HU-10, RN-03, RN-04, RNF-03
CREATE TABLE IF NOT EXISTS visitas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    id_propiedad BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(30) NOT NULL,
    CONSTRAINT fk_visita_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id),
    CONSTRAINT fk_visita_propiedad FOREIGN KEY (id_propiedad) REFERENCES propiedades(id),
    CONSTRAINT fk_visita_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);

-- 5. Tabla de Historial de Cambios de Visita (Auditoría de Cancelaciones y Reprogramaciones) - HU-06, HU-09, RF-11, RN-08
CREATE TABLE IF NOT EXISTS historial_visitas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_visita BIGINT NOT NULL,
    id_usuario_responsable BIGINT NOT NULL,
    tipo_cambio VARCHAR(30) NOT NULL,
    motivo VARCHAR(300) NOT NULL,
    fecha_hora_cambio DATETIME NOT NULL,
    fecha_anterior VARCHAR(40),
    fecha_nueva VARCHAR(40),
    CONSTRAINT fk_historial_visita FOREIGN KEY (id_visita) REFERENCES visitas(id),
    CONSTRAINT fk_historial_usuario FOREIGN KEY (id_usuario_responsable) REFERENCES usuarios(id)
);

-- 6. Tabla de Avisos Internos para Asesores - HU-11, RF-13, RN-10
CREATE TABLE IF NOT EXISTS avisos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    mensaje VARCHAR(400) NOT NULL,
    fecha_hora DATETIME NOT NULL,
    CONSTRAINT fk_aviso_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);

-- ============================================================================
-- CARGA DE DATOS SEMILLA (10 PROPIEDADES CON DESCRIPCIÓN, USUARIOS Y CLIENTES)
-- ============================================================================

-- Usuarios iniciales (Contraseñas cifradas con BCrypt: admin123, carlos123, laura123)
INSERT INTO usuarios (nombres, usuario, contrasena, rol) VALUES
('Admin Principal', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMINISTRADOR'),
('Carlos Asesor', 'carlos', '$2a$10$Dow1z0YqR8O2M6kU8X2v3e1J6s5P8qR9t0U1v2W3x4Y5z6A7B8C9D', 'ASESOR'),
('Laura Asesora', 'laura', '$2a$10$Epx2A1ZrS9P3N7lV9Y3w4f2K7t6Q9rS0u1V2w3X4y5Z6a7B8c9D0E', 'ASESOR');

-- Catálogo de 10 Propiedades con sus respectivas descripciones, tipos, modalidades y estados
INSERT INTO propiedades (codigo_interno, direccion, tipo, modalidad, descripcion, estado) VALUES
('P-01', 'Av. Amazonas N34-120 y Núñez de Vela', 'Casa', 'VENTA', 'Casa amplia de 3 dormitorios, 2 baños y medio, jardín posterior y garaje doble en zona comercial.', 'DISPONIBLE'),
('P-02', 'Av. Shyris N36-45 y Portugal', 'Departamento', 'ALQUILER', 'Departamento moderno frente al parque La Carolina, 2 habitaciones, balcón panorámico y seguridad 24/7.', 'DISPONIBLE'),
('P-03', 'Av. 6 de Diciembre N39-88 y Eloy Alfaro', 'Departamento', 'VENTA', 'Suite ejecutiva en piso 8 con acabados de porcelanato, cocina tipo americana, bodega y 1 parqueadero.', 'DISPONIBLE'),
('P-04', 'Urb. El Condado, Calle C y Paseo de los Eucaliptos', 'Casa', 'VENTA', 'Residencia familiar de 2 plantas, 4 dormitorios con baño privado, área de BBQ, estudio y seguridad privada.', 'DISPONIBLE'),
('P-05', 'Av. González Suárez N27-142 y Orellana', 'Departamento', 'ALQUILER', 'Departamento de lujo de 3 dormitorios con vista al valle, gimnasio comunal, terraza y 2 estacionamientos.', 'DISPONIBLE'),
('P-06', 'Valle de Cumbayá, Av. Interoceánica Km 11', 'Casa', 'ALQUILER', 'Casa independiente en conjunto privado con piscina climatizada, 3 habitaciones, pérgola y áreas verdes.', 'DISPONIBLE'),
('P-07', 'Av. República del Salvador N35-204 y Moscú', 'Departamento', 'VENTA', 'Departamento contemporáneo de 2 dormitorios en zona financiera, edificio inteligente con coworking y terraza.', 'DISPONIBLE'),
('P-08', 'Sector Ponceano Alto, Av. Real Audiencia N62-15', 'Casa', 'VENTA', 'Casa esquinera de 3 pisos, 4 habitaciones, patio amplio, cuarto de máquinas y estacionamiento para 2 vehículos.', 'DISPONIBLE'),
('P-09', 'Av. De los Granados E14-90 y El Inca', 'Casa', 'ALQUILER', 'Casa remodelada de 3 dormitorios dentro de urbanización cerrada, cerca de centros educativos y comerciales.', 'DISPONIBLE'),
('P-10', 'Av. Mariana de Jesús OE7-40 y Pradera', 'Departamento', 'VENTA', 'Departamento iluminado de 3 habitaciones en 5to piso con ascensor, área de lavado y guardianía permanente.', 'RESERVADA');

-- Clientes iniciales
INSERT INTO clientes (identificacion, nombre, apellido, telefono, correo) VALUES
('1723456789', 'Grecco', 'Cedeño', '0991234567', 'grecco@ejemplo.com'),
('1314567890', 'Britany Tahis', 'Macias', '0987654321', 'britany@ejemplo.com'),
('1719876543', 'Mateo', 'Andrade', '0998877665', 'mateo.andrade@ejemplo.com');
