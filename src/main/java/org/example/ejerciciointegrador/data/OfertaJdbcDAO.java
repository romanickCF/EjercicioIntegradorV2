package org.example.ejerciciointegrador.data;



import org.example.ejerciciointegrador.models.Oferta;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link DAO} para {@link Oferta} basada en JDBC.
 * <p>
 * Todas las consultas de lectura hacen un {@code JOIN} con la tabla
 * {@code empresa} para obtener también el nombre de la empresa.
 * </p>
 */
public class OfertaJdbcDAO implements DAO<Oferta> {

    /** Consulta base con el JOIN a empresa. */
    private static final String SELECT_BASE =
            "select o.*, e.nombre as empresa_nombre from Oferta o "
                    + "join empresa e on e.id = o.id_empresa";

    /** Orden por defecto: las candidaturas más recientes primero. */
    private static final String ORDEN = " order by o.fechaPostulacion desc, o.id desc";

    /** Origen de datos utilizado para obtener las conexiones JDBC. */
    private final DataSource ds;

    /**
     * Crea una instancia del DAO.
     *
     * @param ds {@link DataSource} del que se obtendrán las conexiones
     */
    public OfertaJdbcDAO(DataSource ds) {
        this.ds = ds;
    }

    /**
     * Busca una oferta por su identificador.
     *
     * @param id identificador de la oferta
     * @return un {@link Optional} con la oferta, o vacío si no existe
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public Optional<Oferta> findById(Long id) {
        String sql = SELECT_BASE + " where o.id=?";
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
     * Recupera todas las ofertas, de la más reciente a la más antigua.
     *
     * @return lista con todas las ofertas
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public List<Oferta> findAll() {
        return buscar(FiltroOferta.vacio());
    }

    /**
     * Busca ofertas combinando varios criterios a la vez (empresa, estado,
     * salario, fechas y texto libre). Los criterios vacíos se ignoran.
     *
     * @param f criterios de búsqueda
     * @return lista de ofertas que cumplen todos los criterios indicados
     * @throws RuntimeException si ocurre un error de SQL
     */
    public List<Oferta> buscar(FiltroOferta f) {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" where 1=1");
        List<Object> params = new ArrayList<>();

        if (hay(f.texto())) {
            sql.append(" and (o.titulo like ? or o.notas like ?)");
            String patron = "%" + f.texto().trim() + "%";
            params.add(patron);
            params.add(patron);
        }
        if (hay(f.empresa())) {
            sql.append(" and e.nombre like ?");
            params.add("%" + f.empresa().trim() + "%");
        }
        if (hay(f.estado())) {
            sql.append(" and o.estado = ?");
            params.add(f.estado().trim().toLowerCase());
        }
        if (f.salarioMin() != null) {
            sql.append(" and o.salario >= ?");
            params.add(f.salarioMin());
        }
        if (f.salarioMax() != null) {
            sql.append(" and o.salario <= ?");
            params.add(f.salarioMax());
        }
        if (f.desde() != null) {
            sql.append(" and o.fechaPostulacion >= ?");
            params.add(Date.valueOf(f.desde()));
        }
        if (f.hasta() != null) {
            sql.append(" and o.fechaPostulacion <= ?");
            params.add(Date.valueOf(f.hasta()));
        }
        sql.append(ORDEN);

        List<Oferta> ofertas = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ofertas.add(mapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ofertas;
    }

    /**
     * Comprueba si ya existe una oferta con el mismo título, empresa y fecha.
     * Se usa en la importación del CSV para no duplicar candidaturas.
     *
     * @param conn      conexión (y transacción) en la que se ejecuta la consulta
     * @param idEmpresa identificador de la empresa
     * @param titulo    título de la oferta
     * @param fecha     fecha de postulación
     * @return {@code true} si ya existe
     * @throws SQLException si ocurre un error de SQL
     */
    public boolean existe(Connection conn, Long idEmpresa, String titulo, LocalDate fecha) throws SQLException {
        String sql = "select count(*) from oferta where id_empresa=? and titulo=? and fechaPostulacion=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, idEmpresa);
            pst.setString(2, titulo);
            pst.setDate(3, Date.valueOf(fecha));
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Inserta una oferta nueva usando una conexión existente.
     *
     * @param conn conexión (y transacción) en la que se ejecuta el insert
     * @param obj  oferta a guardar
     * @return la oferta con su identificador asignado
     * @throws SQLException si ocurre un error de SQL
     */
    public Oferta save(Connection conn, Oferta obj) throws SQLException {
        String sql = "insert into oferta (id_empresa, titulo, descripcion, estado, "
                + "fechaPostulacion, enlace, salario, notas) values (?,?,?,?,?,?,?,?)";
        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            rellenar(pst, obj);
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
     * Inserta una oferta nueva y le asigna el identificador generado.
     *
     * @param obj oferta a guardar
     * @return la oferta guardada con su identificador
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public Oferta save(Oferta obj) {
        try (Connection conn = ds.getConnection()) {
            return save(conn, obj);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Actualiza todos los datos de una oferta existente.
     *
     * @param entity oferta con los datos actualizados; debe tener identificador
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public void update(Oferta entity) {
        String sql = "update oferta set id_empresa=?, titulo=?, descripcion=?, estado=?, "
                + "fechaPostulacion=?, enlace=?, salario=?, notas=? where id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            rellenar(pst, entity);
            pst.setLong(9, entity.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cambia únicamente el estado de una candidatura.
     *
     * @param id     identificador de la oferta
     * @param estado nuevo estado (pendiente, entrevista, rechazado o aceptado)
     * @throws RuntimeException si ocurre un error de SQL
     */
    public void actualizarEstado(Long id, String estado) {
        String sql = "update oferta set estado=? where id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, estado.trim().toLowerCase());
            pst.setLong(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Elimina una oferta junto con sus entrevistas.
     * <p>
     * Afecta a dos tablas, por lo que se ejecuta dentro de una transacción.
     * </p>
     *
     * @param id identificador de la oferta a eliminar
     * @throws RuntimeException si ocurre un error de SQL
     */
    @Override
    public void deleteById(Long id) {
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement("delete from entrevista where id_oferta=?");
                 PreparedStatement p2 = conn.prepareStatement("delete from oferta where id=?")) {
                p1.setLong(1, id);
                p1.executeUpdate();
                p2.setLong(1, id);
                p2.executeUpdate();
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
     * Asigna a un {@link PreparedStatement} los ocho parámetros comunes de las
     * sentencias insert y update de oferta (en el mismo orden).
     *
     * @param pst sentencia preparada
     * @param o   oferta de la que se toman los valores
     * @throws SQLException si ocurre un error al asignar algún parámetro
     */
    private static void rellenar(PreparedStatement pst, Oferta o) throws SQLException {
        pst.setLong(1, o.getIdEmpresa());
        pst.setString(2, o.getTitulo());
        pst.setString(3, vacioANull(o.getDescripcion()));
        pst.setString(4, o.getEstado().trim().toLowerCase());
        pst.setDate(5, Date.valueOf(o.getFechaPostulacion()));
        pst.setString(6, vacioANull(o.getEnlace()));
        if (o.getSalario() == null) {
            pst.setNull(7, Types.DECIMAL);
        } else {
            pst.setBigDecimal(7, BigDecimal.valueOf(o.getSalario()));
        }
        pst.setString(8, vacioANull(o.getNotas()));
    }

    /**
     * Convierte la fila actual de un {@link ResultSet} en una {@link Oferta}.
     *
     * @param rs resultado posicionado en la fila a mapear (incluye
     *           {@code empresa_nombre} del JOIN)
     * @return la oferta construida a partir de la fila actual
     * @throws SQLException si ocurre un error al leer las columnas
     */
    public static Oferta mapper(ResultSet rs) throws SQLException {
        Oferta o = new Oferta();
        o.setId(rs.getLong("id"));
        o.setIdEmpresa(rs.getLong("id_empresa"));
        o.setEmpresaNombre(rs.getString("empresa_nombre"));
        o.setTitulo(rs.getString("titulo"));
        o.setDescripcion(rs.getString("descripcion"));
        o.setEstado(rs.getString("estado"));
        o.setFechaPostulacion(rs.getDate("fechaPostulacion").toLocalDate());
        o.setEnlace(rs.getString("enlace"));
        BigDecimal salario = rs.getBigDecimal("salario");
        o.setSalario(salario == null ? null : salario.doubleValue());
        o.setNotas(rs.getString("notas"));
        return o;
    }

    /**
     * Indica si un texto contiene algo más que espacios.
     *
     * @param texto texto a comprobar
     * @return {@code true} si no es nulo ni está en blanco
     */
    private static boolean hay(String texto) {
        return texto != null && !texto.isBlank();
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
