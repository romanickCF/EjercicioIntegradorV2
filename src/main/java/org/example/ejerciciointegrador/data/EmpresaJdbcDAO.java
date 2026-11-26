package org.example.ejerciciointegrador.data;


import org.example.ejerciciointegrador.models.Empresa;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link DAO} para {@link Empresa} basada en JDBC.
 * <p>
 * Accede a la tabla {@code empresa}. Algunos métodos tienen una variante que
 * recibe una {@link Connection} para poder participar en una transacción
 * iniciada por otra clase (por ejemplo, la importación del CSV).
 * </p>
 */
public class EmpresaJdbcDAO implements DAO<Empresa> {

    /** Origen de datos utilizado para obtener las conexiones JDBC. */
    private final DataSource ds;

    /**
     * Crea una instancia del DAO.
     *
     * @param ds {@link DataSource} del que se obtendrán las conexiones
     */
    public EmpresaJdbcDAO(DataSource ds) {
        this.ds = ds;
    }

    /**
     * Busca una empresa por su identificador.
     *
     * @param id identificador de la empresa
     * @return un {@link Optional} con la empresa, o vacío si no existe
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public Optional<Empresa> findById(Long id) {
        String sql = "select * from Empresa where id=?";
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
     * Busca una empresa por su nombre exacto usando una conexión existente.
     *
     * @param conn   conexión (y transacción) en la que se ejecuta la consulta
     * @param nombre nombre de la empresa
     * @return un {@link Optional} con la empresa, o vacío si no existe
     * @throws SQLException si ocurre un error de SQL
     */
    public Optional<Empresa> findByNombre(Connection conn, String nombre) throws SQLException {
        String sql = "select * from Empresa where nombre=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, nombre);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Busca una empresa por su nombre exacto.
     *
     * @param nombre nombre de la empresa
     * @return un {@link Optional} con la empresa, o vacío si no existe
     * @throws RuntimeException si ocurre un error de SQL
     */
    public Optional<Empresa> findByNombre(String nombre) {
        try (Connection conn = ds.getConnection()) {
            return findByNombre(conn, nombre);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Recupera todas las empresas ordenadas por nombre.
     *
     * @return lista con todas las empresas
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public List<Empresa> findAll() {
        String sql = "select * from Empresa order by nombre";
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                empresas.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return empresas;
    }

    /**
     * Inserta una empresa nueva usando una conexión existente.
     *
     * @param conn conexión (y transacción) en la que se ejecuta el insert
     * @param obj  empresa a guardar
     * @return la empresa con su identificador asignado
     * @throws SQLException si ocurre un error de SQL
     */
    public Empresa save(Connection conn, Empresa obj) throws SQLException {
        String sql = "insert into Empresa (nombre, sector, web, fechaRegistro) values (?,?,?,?)";
        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, obj.getNombre());
            pst.setString(2, vacioANull(obj.getSector()));
            pst.setString(3, vacioANull(obj.getWeb()));
            pst.setDate(4, Date.valueOf(obj.getFechaRegistro()));
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    obj.setId(rs.getLong(1));
                }
            }
        }
        return obj;
    }

    /**
     * Inserta una empresa nueva y le asigna el identificador generado.
     *
     * @param obj empresa a guardar
     * @return la empresa guardada con su identificador
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public Empresa save(Empresa obj) {
        try (Connection conn = ds.getConnection()) {
            return save(conn, obj);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Actualiza los datos de una empresa existente.
     *
     * @param entity empresa con los datos actualizados; debe tener identificador
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public void update(Empresa entity) {
        String sql = "update Empresa set nombre=?, sector=?, web=?, fechaRegistro=? where id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, entity.getNombre());
            pst.setString(2, vacioANull(entity.getSector()));
            pst.setString(3, vacioANull(entity.getWeb()));
            pst.setDate(4, Date.valueOf(entity.getFechaRegistro()));
            pst.setLong(5, entity.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cuenta las ofertas asociadas a una empresa.
     *
     * @param idEmpresa identificador de la empresa
     * @return número de ofertas de esa empresa
     * @throws RuntimeException si ocurre un error de SQL
     */
    public int contarOfertas(Long idEmpresa) {
        String sql = "select count(*) from Oferta where id_empresa=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, idEmpresa);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Elimina una empresa junto con sus ofertas y las entrevistas de estas.
     * <p>
     * Afecta a tres tablas, por lo que se ejecuta dentro de una transacción:
     * si falla cualquier borrado se deshace todo con {@code rollback}.
     * </p>
     *
     * @param id identificador de la empresa a eliminar
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public void deleteById(Long id) {
        String sqlEntrevistas = "delete from Entrevista where id_oferta in "
                + "(select id from Oferta where id_empresa=?)";
        String sqlOfertas = "delete from Oferta where id_empresa=?";
        String sqlEmpresa = "delete from Empresa where id=?";

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(sqlEntrevistas);
                 PreparedStatement p2 = conn.prepareStatement(sqlOfertas);
                 PreparedStatement p3 = conn.prepareStatement(sqlEmpresa)) {
                p1.setLong(1, id);
                p1.executeUpdate();
                p2.setLong(1, id);
                p2.executeUpdate();
                p3.setLong(1, id);
                p3.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Métodos auxiliares */

    /**
     * Convierte la fila actual de un {@link ResultSet} en una {@link Empresa}.
     *
     * @param rs resultado posicionado en la fila a mapear
     * @return la empresa construida a partir de la fila actual
     * @throws SQLException si ocurre un error al leer las columnas
     */
    public static Empresa mapper(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setId(rs.getLong("id"));
        e.setNombre(rs.getString("nombre"));
        e.setSector(rs.getString("sector"));
        e.setWeb(rs.getString("web"));
        Date fecha = rs.getDate("fechaRegistro");
        e.setFechaRegistro(fecha == null ? null : fecha.toLocalDate());
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
