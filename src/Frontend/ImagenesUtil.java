package Frontend;

import java.io.File;

public class ImagenesUtil {
    private static final String CARPETA_IMAGENES = "imagenes";

    private ImagenesUtil() {
    }

    public static File obtenerCarpetaImagenes() {
        return new File(CARPETA_IMAGENES).getAbsoluteFile();
    }

    public static String guardarRutaImagen(File archivo) {
        if (archivo == null) {
            return "";
        }

        return CARPETA_IMAGENES + File.separator + archivo.getName();
    }

    public static File resolverRutaImagen(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            return null;
        }

        String rutaNormalizada = ruta.trim();
        String prefijo = CARPETA_IMAGENES + File.separator;
        if (new File(rutaNormalizada).isAbsolute() || !rutaNormalizada.startsWith(prefijo)) {
            return null;
        }

        File archivo = new File(rutaNormalizada);
        return archivo.isFile() ? archivo : null;
    }
}
