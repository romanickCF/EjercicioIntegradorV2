package org.example.ejerciciointegrador.models;

import lombok.Data;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Entidad que representa una candidatura (oferta de empleo) del usuario.
 * <p>
 * Se corresponde con la tabla {@code OFERTA} de la base de datos. Los métodos
 * de acceso (getters y setters) se generan automáticamente mediante la
 * anotación {@link lombok.Data} de Lombok.
 * </p>
 * <p>
 * La empresa se guarda en la tabla {@code EMPRESA} y se enlaza por clave
 * ajena ({@link #idEmpresa}). El campo {@link #empresaNombre} no es una
 * columna de {@code OFERTA}: se rellena con un {@code JOIN} para poder mostrar
 * el nombre de la empresa en la tabla sin hacer una consulta adicional.
 * </p>
 */
@Data
public class Oferta {

        /**
         * Formato de fecha usado en el CSV y en la interfaz (dd/MM/yyyy).
         */
        public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        /**
         * Estados permitidos para una candidatura.
         */
        public static final List<String> ESTADOS = List.of("pendiente", "entrevista", "rechazado", "aceptado");

        /**
         * Identificador único de la oferta en la base de datos.
         */
        private Long id;

        /**
         * Identificador de la empresa asociada (clave ajena a {@code EMPRESA}).
         */
        private Long idEmpresa;

        /**
         * Nombre de la empresa asociada. Se obtiene mediante {@code JOIN}; no se persiste.
         */
        private String empresaNombre;

        /**
         * Título del puesto ofertado.
         */
        private String titulo;

        /**
         * Descripción detallada de la oferta (opcional).
         */
        private String descripcion;

        /**
         * Estado de la candidatura: pendiente, entrevista, rechazado o aceptado.
         */
        private String estado;

        /**
         * Fecha en la que se envió la candidatura.
         */
        private LocalDate fechaPostulacion;

        /**
         * Enlace a la oferta original (opcional).
         */
        private String enlace;

        /**
         * Salario ofrecido (opcional, {@code null} si no se conoce).
         */
        private Double salario;

        /**
         * Notas libres del candidato (opcional).
         */
        private String notas;

}