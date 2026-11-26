package org.example.ejerciciointegrador.models;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Entidad que representa una entrevista asociada a una oferta.
 * <p>
 * Se corresponde con la tabla {@code entrevista}. Los métodos de acceso se
 * generan automáticamente mediante {@link lombok.Data}.
 * </p>
 */
@Data
public class Entrevista {

    /** Formato de fecha y hora usado en la interfaz. */
    public static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Tipos de entrevista que se ofrecen en el formulario. */
    public static final List<String> TIPOS = List.of("Telefónica", "RRHH", "Técnica", "Con el equipo", "Final");

    /** Resultados posibles de una entrevista. */
    public static final List<String> RESULTADOS = List.of("Pendiente", "Superada", "No superada");

    /** Identificador único de la entrevista. */
    private Long id;

    /** Identificador de la oferta a la que pertenece (clave ajena). */
    private Long idOferta;

    /** Fecha y hora de la entrevista. */
    private LocalDateTime fecha;

    /** Tipo de entrevista (telefónica, técnica, etc.). */
    private String tipo;

    /** Notas libres sobre la entrevista (opcional). */
    private String notas;

    /** Resultado de la entrevista. */
    private String resultado;

    /**
     * Devuelve la fecha y hora con formato dd/MM/yyyy HH:mm.
     *
     * @return la fecha formateada, o cadena vacía si no hay fecha
     */
    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA_HORA);
    }
}