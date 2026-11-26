package org.example.ejerciciointegrador.models;

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Entidad que representa una empresa a la que se envían candidaturas.
 * <p>
 * Se corresponde con la tabla {@code empresa}. Los métodos de acceso se
 * generan automáticamente mediante {@link Data}.
 * </p>
 */
@Data
public class Empresa {

    /** Formato de fecha usado en la interfaz (dd/MM/yyyy). */
    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Identificador único de la empresa en la base de datos. */
    private Long id;

    /** Nombre de la empresa. */
    private String nombre;

    /** Sector de actividad (opcional). */
    private String sector;

    /** Página web de la empresa (opcional). */
    private String web;

    /** Fecha en la que se registró la empresa en la aplicación. */
    private LocalDate fechaRegistro;

    /**
     * Devuelve la fecha de registro con formato dd/MM/yyyy.
     *
     * @return la fecha formateada, o cadena vacía si no hay fecha
     */
    public String getFechaTexto() {
        return fechaRegistro == null ? "" : fechaRegistro.format(FORMATO_FECHA);
    }

    /**
     * Devuelve el nombre de la empresa, de modo que un {@code ComboBox} la
     * muestre de forma legible.
     *
     * @return el nombre de la empresa
     */
    @Override
    public String toString() {
        return nombre;
    }
}
