package org.example.ejerciciointegrador.common;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.ejerciciointegrador.App;
import org.example.ejerciciointegrador.controllers.*;
import org.example.ejerciciointegrador.data.EmpresaJdbcDAO;
import org.example.ejerciciointegrador.data.EntrevistaJdbcDAO;
import org.example.ejerciciointegrador.data.OfertaJdbcDAO;
import org.example.ejerciciointegrador.service.CsvImportService;

import java.io.IOException;
import java.net.URL;

/**
 * Clase utilitaria para navegar entre las vistas FXML de la aplicación.
 * <p>
 * Mantiene una única referencia al {@link Stage} compartido y cambia la escena
 * mostrada en él cada vez que se solicita una nueva vista. Crea los
 * controladores mediante una factoría para inyectarles por constructor los DAO
 * y servicios que necesitan.
 * </p>
 */
public class SceneNavigator {

    /** Escenario principal de la aplicación sobre el que se muestran las escenas. */
    private static Stage stage;

    /** DAO de ofertas compartido por los controladores. */
    private static OfertaJdbcDAO ofertaDAO;

    /** DAO de empresas compartido por los controladores. */
    private static EmpresaJdbcDAO empresaDAO;

    /** DAO de entrevistas compartido por los controladores. */
    private static EntrevistaJdbcDAO entrevistaDAO;

    /** Servicio de importación de CSV compartido por los controladores. */
    private static CsvImportService importService;

    /**
     * Registra el escenario que se utilizará en las siguientes navegaciones.
     *
     * @param stage el {@link Stage} principal de la aplicación
     */
    public static void setStage(Stage stage) {
        SceneNavigator.stage = stage;
    }

    /**
     * Registra los DAO y servicios que se inyectarán en los controladores.
     *
     * @param ofertaDAO     DAO de ofertas
     * @param empresaDAO    DAO de empresas
     * @param entrevistaDAO DAO de entrevistas
     * @param importService servicio de importación de CSV
     */
    public static void setServicios(OfertaJdbcDAO ofertaDAO, EmpresaJdbcDAO empresaDAO,
                                    EntrevistaJdbcDAO entrevistaDAO, CsvImportService importService) {
        SceneNavigator.ofertaDAO = ofertaDAO;
        SceneNavigator.empresaDAO = empresaDAO;
        SceneNavigator.entrevistaDAO = entrevistaDAO;
        SceneNavigator.importService = importService;
    }

    /**
     * Carga la vista FXML indicada y la muestra en el escenario registrado,
     * creando su controlador con las dependencias necesarias.
     * <p>
     * El fichero FXML se busca en el paquete {@code views/} de {@link App}. La
     * nueva escena conserva el tamaño de la anterior.
     * </p>
     *
     * @param fxml  nombre del fichero FXML a cargar (relativo a {@code views/})
     * @param title título de la ventana que se asignará al escenario
     * @throws RuntimeException si no se puede cargar el recurso FXML (envuelve
     *                          la {@link IOException})
     * @throws IllegalArgumentException si el controlador de la vista no está soportado
     */
    public static void navigateTo(String fxml, String title) {
        URL url = App.class.getResource("views/" + fxml);
        if (url == null) {
            throw new IllegalStateException("No se encuentra la vista " + fxml);
        }
        FXMLLoader fxmlLoader = new FXMLLoader(url);

        fxmlLoader.setControllerFactory(type -> {
            if (type == OfertasController.class) {
                return new OfertasController(ofertaDAO, importService);
            } else if (type == OfertaDetailController.class) {
                return new OfertaDetailController(ofertaDAO, empresaDAO, entrevistaDAO);
            } else if (type == OfertaFormController.class) {
                return new OfertaFormController(ofertaDAO, empresaDAO);
            } else if (type == EmpresasController.class) {
                return new EmpresasController(empresaDAO);
            } else if (type == EmpresaFormController.class) {
                return new EmpresaFormController(empresaDAO);
            } else if (type == EntrevistaFormController.class) {
                return new EntrevistaFormController(entrevistaDAO);
            }
            throw new IllegalArgumentException("Controlador no soportado: " + type.getName());
        });

        try {
            Parent root = fxmlLoader.load();
            Scene anterior = stage.getScene();
            Scene scene = (anterior == null)
                    ? new Scene(root, 1050, 680)
                    : new Scene(root, anterior.getWidth(), anterior.getHeight());
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
