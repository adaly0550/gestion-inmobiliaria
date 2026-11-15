import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Lanzador directo para IntelliJ IDEA (Community y Ultimate Edition).
 *
 * Permite ejecutar el proyecto Node.js (server.js) directamente con el botón
 * verde "Run (▶)" de IntelliJ IDEA sin modificar ninguna regla de negocio ni
 * archivo original del sistema.
 */
public class IniciarServidor {
    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("  INICIANDO SISTEMA DE GESTIÓN INMOBILIARIA (IDE)   ");
        System.out.println("====================================================");

        try {
            File directorioProyecto = new File(System.getProperty("user.dir"));
            ProcessBuilder builder = new ProcessBuilder("node", "server.js");
            builder.directory(directorioProyecto);
            builder.redirectErrorStream(true);

            Process proceso = builder.start();

            // Si detienes el programa con el botón rojo Stop (⏹) de IntelliJ,
            // se cierra automáticamente el proceso de Node para liberar el puerto 3000.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (proceso.isAlive()) {
                    System.out.println("\nDeteniendo servidor desde IntelliJ IDEA...");
                    proceso.destroyForcibly();
                }
            }));

            try (BufferedReader lector = new BufferedReader(
                    new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = lector.readLine()) != null) {
                    System.out.println(linea);
                }
            }

            int codigoSalida = proceso.waitFor();
            System.out.println("El servidor finalizó con código: " + codigoSalida);

        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor con Node.js: " + e.getMessage());
            System.err.println("Verifica que Node.js esté instalado y en el PATH del sistema.");
        }
    }
}
