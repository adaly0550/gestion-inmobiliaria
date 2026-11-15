// server.js
import app from './src/app.js';
import { conectarDB } from './src/config/database.js';

const PORT = process.env.PORT || 3000;

// Primero conectamos la base de datos SQLite y luego encendemos el servidor
conectarDB()
  .then(() => {
    app.listen(PORT, () => {
      console.log(`Servidor de práctica corriendo en http://localhost:${PORT}`);
      console.log(`Abre tu navegador en: http://localhost:${PORT}`);
    });
  })
  .catch((error) => {
    console.error('Error al iniciar la base de datos:', error.message);
  });
