package org.example.ejerciciointegrador;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.example.ejerciciointegrador.common.Alertas;
import org.example.ejerciciointegrador.common.SceneNavigator;
import org.example.ejerciciointegrador.data.*;
import org.example.ejerciciointegrador.service.CsvImportService;

import javax.sql.DataSource;

/**
 * Clase principal de la aplicación JavaFX.
 * <p>
 * Prepara la base de datos (creación de las tablas), crea los DAO y el
 * servicio de importación, los registra en el {@link SceneNavigator} para
 * inyectarlos en los controladores y muestra la pantalla de candidaturas.
 * </p>
 */
public class App extends Application {

    /**
     * Punto de entrada de JavaFX. Se ejecuta automáticamente al arrancar la
     * aplicación.
     *
     * @param stage escenario principal que proporciona JavaFX y sobre el que se
     *              mostrarán las distintas vistas
     */
    @Override
    public void start(Stage stage) {
        DataSource ds;
        try {
            ds = DataSourceFactory.getDataSource();
            DataBaseInitializer.initialize(ds);
        } catch (RuntimeException e) {
            Alertas.error("No se pudo conectar con la base de datos.\n"
                    + "Revisa config.properties y que MySQL esté en marcha.\n\n" + e.getMessage());
            Platform.exit();
            return;
        }

        EmpresaJdbcDAO empresaDAO = new EmpresaJdbcDAO(ds);
        OfertaJdbcDAO ofertaDAO = new OfertaJdbcDAO(ds);
        EntrevistaJdbcDAO entrevistaDAO = new EntrevistaJdbcDAO(ds);
        CsvImportService importService = new CsvImportService(ds, empresaDAO, ofertaDAO);

        SceneNavigator.setStage(stage);
        SceneNavigator.setServicios(ofertaDAO, empresaDAO, entrevistaDAO, importService);
        SceneNavigator.navigateTo("ofertas-view.fxml", "Candidaturas");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }
}
