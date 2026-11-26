package org.example.ejerciciointegrador.data;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Utilidad para inicializar la base de datos de la aplicación.
 * <p>
 * Ejecuta el script {@code schema.sql} incluido en los recursos para crear
 * las tablas si no existen.
 * </p>
 */
public class DataBaseInitializer {

    /**
     * Crea la estructura de la base de datos ejecutando {@code schema.sql}.
     *
     * @param ds origen de datos sobre el que se ejecuta el script
     * @throws RuntimeException si el script no existe o falla alguna sentencia
     */
    public static void initialize(DataSource ds) {
        executeScript(ds, "schema.sql");
    }

    /**
     * Ejecuta todas las sentencias de un script SQL ubicado en los recursos.
     * <p>
     * Lee el fichero completo, ignora las líneas de comentario, lo divide por
     * el carácter {@code ;} y ejecuta cada sentencia no vacía.
     * </p>
     *
     * @param ds           origen de datos sobre el que se ejecuta el script
     * @param resourceName nombre del fichero SQL dentro de los recursos
     * @throws RuntimeException si el recurso no existe o si ocurre un error de
     *                          entrada/salida o de SQL
     */
    private static void executeScript(DataSource ds, String resourceName) {
        try (InputStream is = DataBaseInitializer.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new RuntimeException("No se encontró el recurso " + resourceName);
            }

            String sql;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                sql = reader.lines()
                        .filter(linea -> !linea.trim().startsWith("--"))
                        .collect(Collectors.joining("\n"));
            }

            try (Connection conn = ds.getConnection();
                 Statement st = conn.createStatement()) {
                for (String statement : sql.split(";")) {
                    if (!statement.isBlank()) {
                        st.execute(statement);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
