package org.example.ejerciciointegrador.service;


import org.example.ejerciciointegrador.data.EmpresaJdbcDAO;
import org.example.ejerciciointegrador.data.OfertaJdbcDAO;
import org.example.ejerciciointegrador.models.Empresa;
import org.example.ejerciciointegrador.models.Oferta;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Servicio que importa candidaturas desde un fichero CSV a la base de datos.
 * <p>
 * Formato esperado (con cabecera):
 * {@code id;titulo;empresa;estado;fechaPostulacion;enlace;notas}.
 * Por cada fila se busca la empresa por nombre (y se crea si no existe) y se
 * inserta la oferta asociada. Todo el proceso se ejecuta en una única
 * transacción: si falla algo no se guarda nada.
 * </p>
 */
public class CsvImportService {

    /** Cabecera esperada en la primera línea del CSV. */
    private static final String CABECERA = "id;titulo;empresa;estado;fechapostulacion;enlace;notas";

    /** Origen de datos para abrir la conexión transaccional. */
    private final DataSource ds;

    /** DAO de empresas. */
    private final EmpresaJdbcDAO empresaDAO;

    /** DAO de ofertas. */
    private final OfertaJdbcDAO ofertaDAO;

    /**
     * Crea el servicio inyectando sus dependencias.
     *
     * @param ds         origen de datos
     * @param empresaDAO DAO de empresas
     * @param ofertaDAO  DAO de ofertas
     */
    public CsvImportService(DataSource ds, EmpresaJdbcDAO empresaDAO, OfertaJdbcDAO ofertaDAO) {
        this.ds = ds;
        this.empresaDAO = empresaDAO;
        this.ofertaDAO = ofertaDAO;
    }

    /**
     * Importa el fichero CSV indicado.
     *
     * @param fichero ruta del fichero CSV
     * @return número de candidaturas nuevas añadidas (las que ya existían se omiten)
     * @throws IOException              si no se puede leer el fichero
     *                                  ({@link java.nio.file.NoSuchFileException} si no existe)
     * @throws IllegalArgumentException si el CSV está vacío, tiene la cabecera
     *                                  incorrecta o alguna línea es inválida
     * @throws RuntimeException         si ocurre un error de SQL (se hace rollback)
     */
    public int importar(Path fichero) throws IOException {
        List<String> lineas = Files.readAllLines(fichero, StandardCharsets.UTF_8);
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("El fichero está vacío.");
        }

        String cabecera = lineas.get(0).replace("\uFEFF", "").trim().toLowerCase();
        if (!cabecera.equals(CABECERA)) {
            throw new IllegalArgumentException("Cabecera incorrecta. Se esperaba: id;titulo;empresa;estado;fechaPostulacion;enlace;notas");
        }

        // Se valida todo el fichero antes de tocar la base de datos
        List<Oferta> ofertas = new ArrayList<>();
        for (int i = 1; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank()) {
                continue;
            }
            try {
                ofertas.add(Oferta.desdeLineaCsv(linea));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Línea " + (i + 1) + ": " + e.getMessage());
            }
        }

        return guardar(ofertas);
    }

    /**
     * Guarda las ofertas (y sus empresas) en una única transacción.
     *
     * @param ofertas ofertas ya validadas
     * @return número de ofertas nuevas insertadas
     */
    private int guardar(List<Oferta> ofertas) {
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Map<String, Empresa> cache = new HashMap<>();
                int nuevas = 0;

                for (Oferta oferta : ofertas) {
                    String nombre = oferta.getEmpresaNombre();
                    Empresa empresa = cache.get(nombre.toLowerCase());

                    if (empresa == null) {
                        Optional<Empresa> existente = empresaDAO.findByNombre(conn, nombre);
                        if (existente.isPresent()) {
                            empresa = existente.get();
                        } else {
                            empresa = new Empresa();
                            empresa.setNombre(nombre);
                            empresa.setFechaRegistro(LocalDate.now());
                            empresaDAO.save(conn, empresa);
                        }
                        cache.put(nombre.toLowerCase(), empresa);
                    }

                    oferta.setIdEmpresa(empresa.getId());
                    if (!ofertaDAO.existe(conn, empresa.getId(), oferta.getTitulo(), oferta.getFechaPostulacion())) {
                        ofertaDAO.save(conn, oferta);
                        nuevas++;
                    }
                }

                conn.commit();
                return nuevas;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
