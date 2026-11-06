package Backend;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class GeminiAPIClient {
    private static final String API_KEY_FILE = "gemini-api-key.txt";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-3.6-flash:generateContent?key=";

    private GeminiAPIClient() {
    }

    public static String callGeminiAPI(String prompt) throws IOException {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }

        String apiKey = Files.readString(
                java.nio.file.Paths.get(API_KEY_FILE),
                StandardCharsets.UTF_8
        ).trim();
        if (apiKey.isEmpty() || apiKey.matches("0+")) {
            throw new IOException("Debes colocar una API key válida en " + API_KEY_FILE + ".");
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(API_URL + apiKey).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.setDoOutput(true);

        String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\""
                + escapeJson(prompt) + "\"}]}]}";

        try (OutputStream output = connection.getOutputStream()) {
            output.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String detalle = "";
                if (connection.getErrorStream() != null) {
                    detalle = new String(
                            connection.getErrorStream().readAllBytes(),
                            StandardCharsets.UTF_8
                    );
                }
                throw new IOException("Gemini respondió con el código " + responseCode
                        + (detalle.isEmpty() ? "." : ": " + detalle));
            }

            String response = new String(
                    connection.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
            return extractTextFromResponse(response);
        } finally {
            connection.disconnect();
        }
    }

    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String extractTextFromResponse(String json) throws IOException {
        int keyStart = json.indexOf("\"text\"");
        if (keyStart < 0) {
            throw new IOException("La respuesta de Gemini no contiene texto.");
        }

        int valueStart = json.indexOf('"', json.indexOf(':', keyStart) + 1) + 1;
        if (valueStart <= 0) {
            throw new IOException("La respuesta de Gemini tiene un formato inesperado.");
        }

        StringBuilder text = new StringBuilder();
        boolean escaped = false;
        for (int i = valueStart; i < json.length(); i++) {
            char character = json.charAt(i);
            if (escaped) {
                switch (character) {
                    case 'n' -> text.append('\n');
                    case 'r' -> text.append('\r');
                    case 't' -> text.append('\t');
                    case '"' -> text.append('"');
                    case '\\' -> text.append('\\');
                    case 'b' -> text.append('\b');
                    case 'f' -> text.append('\f');
                    case 'u' -> {
                        if (i + 4 >= json.length()) {
                            throw new IOException("La respuesta de Gemini tiene un formato inesperado.");
                        }
                        String unicode = json.substring(i + 1, i + 5);
                        try {
                            text.append((char) Integer.parseInt(unicode, 16));
                            i += 4;
                        } catch (NumberFormatException exception) {
                            throw new IOException("La respuesta de Gemini tiene un formato inesperado.", exception);
                        }
                    }
                    default -> text.append(character);
                }
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == '"') {
                return text.toString();
            } else {
                text.append(character);
            }
        }
        throw new IOException("La respuesta de Gemini tiene un formato inesperado.");
    }
}
