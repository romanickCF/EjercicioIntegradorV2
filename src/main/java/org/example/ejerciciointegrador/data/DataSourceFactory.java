package org.example.ejerciciointegrador.data;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DataSourceFactory {

    private static DataSource dataSource = null;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            Properties p = new Properties();
            try (InputStream is = DataSourceFactory.class.getClassLoader()
                    .getResourceAsStream("config.properties")) {
                if (is != null) p.load(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String url  = System.getenv().getOrDefault("DB_URL",  p.getProperty("db.url"));
            String user = System.getenv().getOrDefault("DB_USER", p.getProperty("db.user"));
            String pass = System.getenv().getOrDefault("DB_PASSWORD", p.getProperty("db.password"));

            MysqlDataSource ds = new MysqlDataSource();
            ds.setURL(url);
            ds.setUser(user);
            ds.setPassword(pass);
            dataSource = ds;
        }
        return dataSource;
    }
}
