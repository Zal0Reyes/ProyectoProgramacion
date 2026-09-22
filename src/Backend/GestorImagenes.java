package Backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class GestorImagenes {
    private static final Path CARPETA_IMAGENES = Paths.get("imagenes");

    private GestorImagenes() {
    }

    public static String copiarARepositorio(File archivoOrigen) throws IOException {
        Files.createDirectories(CARPETA_IMAGENES);
        Path destino = CARPETA_IMAGENES.resolve(archivoOrigen.getName());
        Files.copy(archivoOrigen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return archivoOrigen.getName();
    }

    public static File resolver(String referencia) {
        if (referencia == null || referencia.trim().isEmpty()) {
            return null;
        }

        String nombreArchivo = referencia.trim();
        if (!esNombreDeArchivo(nombreArchivo)) {
            return null;
        }

        File archivoLocal = CARPETA_IMAGENES.resolve(nombreArchivo).toFile();
        return archivoLocal.isFile() ? archivoLocal : null;
    }

    public static String normalizarReferencia(String referencia) {
        if (referencia == null || referencia.trim().isEmpty()) {
            return "";
        }

        String nombreArchivo = referencia.trim();
        return esNombreDeArchivo(nombreArchivo) ? nombreArchivo : "";
    }

    private static boolean esNombreDeArchivo(String referencia) {
        if (new File(referencia).isAbsolute()) {
            return false;
        }

        return !referencia.contains("\\") && !referencia.contains("/");
    }
}