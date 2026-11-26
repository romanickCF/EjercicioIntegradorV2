package org.example.ejerciciointegrador.data;



import lombok.RequiredArgsConstructor;
import org.example.ejerciciointegrador.models.Entrevista;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link DAO} para {@link Entrevista} basada en JDBC.
 * <p>
 * Accede a la tabla {@code entrevista}, relacionada con {@code oferta} por la
 * clave ajena {@code id_oferta}.
 * </p>
 */
@RequiredArgsConstructor
public class EntrevistaJdbcDAO implements DAO<Entrevista> {

    /** Origen de datos utilizado para obtener las conexiones JDBC. */
    private final DataSource ds;

    /**
     * Busca una entrevista por su identificador.
     *
     * @param id identificador de la entrevista
     * @return un {@link Optional} con la entrevista, o vacío si no existe
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public Optional<Entrevista> findById(Long id) {
        String sql = "select * from entrevista where id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /**
     * Recupera todas las entrevistas ordenadas por fecha.
     *
     * @return lista con todas las entrevistas
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public List<Entrevista> findAll() {
        return consultar("select * from entrevista order by fecha", null);
    }

    /**
     * Recupera las entrevistas de una oferta, ordenadas por fecha.
     *
     * @param idOferta identificador de la oferta
     * @return lista de entrevistas de esa oferta
     * @throws RuntimeException si ocurre un error de SQL
     */
    public List<Entrevista> findByOferta(Long idOferta) {
        return consultar("select * from entrevista where id_oferta=? order by fecha", idOferta);
    }

    /**
     * Inserta una entrevista nueva y le asigna el identificador generado.
     *
     * @param obj entrevista a guardar
     * @return la entrevista guardada con su identificador
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public Entrevista save(Entrevista obj) {
        String sql = "insert into entrevista (id_oferta, fecha, tipo, notas, resultado) values (?,?,?,?,?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setLong(1, obj.getIdOferta());
            pst.setTimestamp(2, Timestamp.valueOf(obj.getFecha()));
            pst.setString(3, obj.getTipo());
            pst.setString(4, vacioANull(obj.getNotas()));
            pst.setString(5, obj.getResultado());
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    obj.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    /**
     * Actualiza los datos de una entrevista existente.
     *
     * @param entity entrevista con los datos actualizados; debe tener identificador
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public void update(Entrevista entity) {
        String sql = "update entrevista set fecha=?, tipo=?, notas=?, resultado=? where id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(entity.getFecha()));
            pst.setString(2, entity.getTipo());
            pst.setString(3, vacioANull(entity.getNotas()));
            pst.setString(4, entity.getResultado());
            pst.setLong(5, entity.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Elimina la entrevista con el identificador indicado.
     *
     * @param id identificador de la entrevista a eliminar
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public void deleteById(Long id) {
        String sql = "delete from entrevista where id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Métodos auxiliares */

    /**
     * Ejecuta una consulta de entrevistas con, como mucho, un parámetro.
     *
     * @param sql   consulta a ejecutar
     * @param param valor del único parámetro, o {@code null} si no hay
     * @return la lista de entrevistas obtenida
     */
    private List<Entrevista> consultar(String sql, Long param) {
        List<Entrevista> lista = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            if (param != null) {
                pst.setLong(1, param);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    /**
     * Convierte la fila actual de un {@link ResultSet} en una {@link Entrevista}.
     *
     * @param rs resultado posicionado en la fila a mapear
     * @return la entrevista construida a partir de la fila actual
     * @throws SQLException si ocurre un error al leer las columnas
     */
    public static Entrevista mapper(ResultSet rs) throws SQLException {
        Entrevista e = new Entrevista();
        e.setId(rs.getLong("id"));
        e.setIdOferta(rs.getLong("id_oferta"));
        e.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        e.setTipo(rs.getString("tipo"));
        e.setNotas(rs.getString("notas"));
        e.setResultado(rs.getString("resultado"));
        return e;
    }

    /**
     * Convierte un texto vacío en {@code null} para guardarlo como NULL en BD.
     *
     * @param texto texto a comprobar
     * @return el texto recortado, o {@code null} si es nulo o está en blanco
     */
    private static String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
