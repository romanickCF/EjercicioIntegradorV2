package org.example.ejerciciointegrador.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CsvImportServiceTest {

    private static final String CABECERA = "id;titulo;empresa;estado;fechaPostulacion;enlace;notas";

    /** La validación ocurre antes de usar el DataSource, así que null es suficiente aquí. */
    private final CsvImportService servicio = new CsvImportService(null, null, null);

    @TempDir
    Path temporal;

    @Test
    void importarRechazaFicheroVacio() throws IOException {
        Path csv = fichero("");
        assertThrows(IllegalArgumentException.class, () -> servicio.importar(csv));
    }

    @Test
    void importarRechazaCabeceraIncorrecta() throws IOException {
        Path csv = fichero("id;titulo;empresa\n1;Titulo;Indra\n");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> servicio.importar(csv));
        assertTrue(e.getMessage().contains("Cabecera incorrecta"));
    }

    @Test
    void importarAceptaCabeceraEnMayusculasConBom() throws IOException {
        Path csv = fichero("\uFEFF" + CABECERA.toUpperCase(Locale.ROOT) + "\n1;;Indra;pendiente;26/10/2026;;\n");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> servicio.importar(csv));
        assertTrue(e.getMessage().contains("Línea 2"), "La cabecera se aceptó; falla la fila inválida");
    }

    @Test
    void importarInformaDeLaLineaInvalida() throws IOException {
        Path csv = fichero(CABECERA + "\n"
                + "1;Titulo valido;Indra;pendiente;26/10/2026;;\n"
                + "2;Titulo;Indra;estado-raro;27/10/2026;;\n");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> servicio.importar(csv));
        assertTrue(e.getMessage().startsWith("Línea 3"));
        assertTrue(e.getMessage().contains("Estado no válido"));
    }

    private Path fichero(String contenido) throws IOException {
        Path csv = temporal.resolve("candidaturas.csv");
        Files.writeString(csv, contenido, StandardCharsets.UTF_8);
        return csv;
    }
}
