package org.example.ejerciciointegrador.data;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Fábrica encargada de crear y proporcionar un {@link DataSource} para la
 * base de datos MySQL.
 * <p>
 * Implementa un singleton perezoso: el {@link DataSource} se crea la primera
 * vez que se invoca {@link #getDataSource()} y se reutiliza después. La
 * configuración (URL, usuario y contraseña) se lee del fichero
 * {@code config.properties}; las variables de entorno {@code DB_URL},
 * {@code DB_USER} y {@code DB_PASSWORD}, si existen, tienen prioridad.
 * </p>
 */
public class DataSourceFactory {

    /** Instancia única del {@link DataSource}, creada de forma perezosa. */
    private static DataSource dataSource = null;

    /**
     * Devuelve el {@link DataSource} compartido, creándolo si aún no existe.
     *
     * @return el {@link DataSource} configurado para conectar con MySQL
     * @throws RuntimeException si no se puede leer la configuración o falta
     *                          alguno de los datos obligatorios
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            Properties props = new Properties();
            try (InputStream is = DataSourceFactory.class.getClassLoader()
                    .getResourceAsStream("config.properties")) {
                if (is != null) {
                    props.load(is);
                }
            } catch (IOException e) {
                throw new RuntimeException("No se pudo leer config.properties", e);
            }

            String url = valor("DB_URL", props.getProperty("db.url"));
            String user = valor("DB_USER", props.getProperty("db.user"));
            String password = valor("DB_PASSWORD", props.getProperty("db.password", ""));

            if (url == null || user == null) {
                throw new RuntimeException("Falta la configuración de la base de datos (db.url y db.user)");
            }

            MysqlDataSource ds = new MysqlDataSource();
            ds.setURL(url);
            ds.setUser(user);
            ds.setPassword(password);
            dataSource = ds;
        }
        return dataSource;
    }

    /**
     * Devuelve el valor de una variable de entorno o, si no está definida, el
     * valor por defecto.
     *
     * @param variable     nombre de la variable de entorno
     * @param porDefecto   valor a usar si la variable no existe
     * @return el valor resultante
     */
    private static String valor(String variable, String porDefecto) {
        String env = System.getenv(variable);
        return (env != null && !env.isBlank()) ? env : porDefecto;
    }
}
