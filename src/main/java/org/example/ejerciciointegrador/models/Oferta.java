package org.example.ejerciciointegrador.models;

import lombok.Data;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

        /**
         * Comprueba si un texto corresponde a un estado permitido.
         *
         * @param estado texto a comprobar (no distingue mayúsculas de minúsculas)
         * @return {@code true} si es uno de los estados de {@link #ESTADOS}
         */
        public static boolean esEstadoValido(String estado) {
                return estado != null && ESTADOS.contains(estado.trim().toLowerCase());
        }

        /**
         * Construye una oferta a partir de una línea del CSV de candidaturas.
         * <p>
         * Formato esperado:
         * {@code id;titulo;empresa;estado;fechaPostulacion;enlace;notas}.
         * El identificador del CSV se ignora (lo asigna la base de datos) y el
         * salario queda a {@code null} porque el CSV no lo incluye. La oferta
         * devuelta solo tiene {@link #empresaNombre}; el servicio de importación
         * debe resolver o crear la empresa para asignar {@link #idEmpresa}.
         * </p>
         *
         * @param linea línea del CSV (sin la cabecera)
         * @return la oferta construida
         * @throws IllegalArgumentException si faltan columnas, hay campos
         *                                  obligatorios vacíos, el estado no es
         *                                  válido o la fecha no tiene el formato
         *                                  dd/MM/yyyy
         */
        public static Oferta desdeLineaCsv(String linea) {
                String[] c = linea.split(";", -1); // -1 conserva los campos vacíos finales
                if (c.length < 7) {
                        throw new IllegalArgumentException("Se esperaban 7 columnas y hay " + c.length + ": " + linea);
                }

                String titulo = c[1].trim();
                String empresa = c[2].trim();
                String estado = c[3].trim().toLowerCase();

                if (titulo.isEmpty() || empresa.isEmpty()) {
                        throw new IllegalArgumentException("Título y empresa son obligatorios: " + linea);
                }
                if (!esEstadoValido(estado)) {
                        throw new IllegalArgumentException("Estado no válido '" + c[3] + "' en: " + linea);
                }

                LocalDate fecha;
                try {
                        fecha = LocalDate.parse(c[4].trim(), FORMATO_FECHA);
                } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Fecha no válida '" + c[4] + "' (usa dd/MM/yyyy) en: " + linea);
                }

                Oferta oferta = new Oferta();
                oferta.setTitulo(titulo);
                oferta.setEmpresaNombre(empresa);
                oferta.setEstado(estado);
                oferta.setFechaPostulacion(fecha);
                oferta.setEnlace(c[5].trim());
                oferta.setNotas(c[6].trim());
                return oferta;
        }

        /**
         * Devuelve la fecha de postulación con formato dd/MM/yyyy, lista para
         * mostrarse en la tabla o en el detalle.
         *
         * @return la fecha formateada, o cadena vacía si no hay fecha
         */
        public String getFechaTexto() {
                return fechaPostulacion == null ? "" : fechaPostulacion.format(FORMATO_FECHA);
        }

        /**
         * Devuelve el salario como texto para la interfaz.
         *
         * @return el salario con dos decimales y símbolo del euro, o {@code "--"}
         *         si no se conoce
         */
        public String getSalarioTexto() {
                return salario == null ? "--" : String.format("%.2f €", salario);
        }
}
