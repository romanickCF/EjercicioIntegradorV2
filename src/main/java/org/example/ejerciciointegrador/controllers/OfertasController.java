package org.example.ejerciciointegrador.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.stage.FileChooser;
import org.example.ejerciciointegrador.common.Alertas;
import org.example.ejerciciointegrador.common.AppContext;
import org.example.ejerciciointegrador.common.SceneNavigator;
import org.example.ejerciciointegrador.data.FiltroOferta;
import org.example.ejerciciointegrador.data.OfertaJdbcDAO;
import org.example.ejerciciointegrador.models.Oferta;
import org.example.ejerciciointegrador.service.CsvImportService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla principal: listado de candidaturas.
 * <p>
 * Muestra las ofertas en una tabla, permite filtrarlas combinando varios
 * criterios, cambiar el estado de una candidatura directamente desde la tabla,
 * navegar al detalle, crear, editar o borrar ofertas e importar el CSV inicial.
 * </p>
 */
public class OfertasController implements Initializable {

    /** Texto del selector de estado que significa "sin filtrar por estado". */
    private static final String TODOS = "Todos";

    /** Campo de texto libre (título o notas). */
    @FXML
    private TextField txtTexto;

    /** Campo de filtro por empresa. */
    @FXML
    private TextField txtEmpresa;

    /** Selector del estado por el que filtrar. */
    @FXML
    private ComboBox<String> cmbEstado;

    /** Campo de salario mínimo. */
    @FXML
    private TextField txtSalarioMin;

    /** Campo de salario máximo. */
    @FXML
    private TextField txtSalarioMax;

    /** Selector de fecha de postulación inicial. */
    @FXML
    private DatePicker dpDesde;

    /** Selector de fecha de postulación final. */
    @FXML
    private DatePicker dpHasta;

    /** Tabla con el listado de ofertas. */
    @FXML
    private TableView<Oferta> table;

    /** Columna del identificador. */
    @FXML
    private TableColumn<Oferta, String> cId;

    /** Columna del título. */
    @FXML
    private TableColumn<Oferta, String> cTitulo;

    /** Columna de la empresa. */
    @FXML
    private TableColumn<Oferta, String> cEmpresa;

    /** Columna del estado (editable con un desplegable). */
    @FXML
    private TableColumn<Oferta, String> cEstado;

    /** Columna del salario. */
    @FXML
    private TableColumn<Oferta, String> cSalario;

    /** Columna de la fecha de postulación. */
    @FXML
    private TableColumn<Oferta, String> cFecha;

    /** Etiqueta con el número de resultados. */
    @FXML
    private Label lblTotal;

    /** DAO utilizado para consultar y modificar las ofertas. */
    private final OfertaJdbcDAO ofertaDAO;

    /** Servicio utilizado para importar el CSV. */
    private final CsvImportService importService;

    /**
     * Crea el controlador inyectando sus dependencias.
     *
     * @param ofertaDAO     DAO de ofertas
     * @param importService servicio de importación de CSV
     */
    public OfertasController(OfertaJdbcDAO ofertaDAO, CsvImportService importService) {
        this.ofertaDAO = ofertaDAO;
        this.importService = importService;
    }

    /**
     * Se invoca automáticamente después de cargar el FXML.
     * <p>
     * Configura las columnas de la tabla, el desplegable de estado de cada fila
     * (HU-12), el selector de filtro y carga todas las ofertas.
     * </p>
     *
     * @param location  ubicación usada para resolver rutas relativas, o {@code null}
     * @param resources recursos usados para localizar la vista, o {@code null}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbEstado.getItems().add(TODOS);
        cmbEstado.getItems().addAll(Oferta.ESTADOS);
        cmbEstado.getSelectionModel().selectFirst();

        cId.setCellValueFactory(row -> new SimpleStringProperty(String.valueOf(row.getValue().getId())));
        cTitulo.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getTitulo()));
        cEmpresa.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getEmpresaNombre()));
        cEstado.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getEstado()));
        cSalario.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getSalarioTexto()));
        cFecha.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getFechaTexto()));

        // HU-12: cambiar el estado desde un desplegable en la propia tabla
        table.setEditable(true);
        cEstado.setCellFactory(ComboBoxTableCell.forTableColumn(
                FXCollections.observableArrayList(Oferta.ESTADOS)));
        cEstado.setOnEditCommit(event -> {
            Oferta oferta = event.getRowValue();
            String nuevo = event.getNewValue();
            if (nuevo != null && !nuevo.equals(oferta.getEstado())) {
                try {
                    ofertaDAO.actualizarEstado(oferta.getId(), nuevo);
                    oferta.setEstado(nuevo);
                } catch (RuntimeException e) {
                    Alertas.error("No se pudo cambiar el estado: " + e.getMessage());
                }
            }
            table.refresh();
        });

        cargar();
    }

    /**
     * Aplica los filtros indicados en el formulario y actualiza la tabla.
     *
     * @param actionEvent evento de acción generado al pulsar el botón filtrar
     */
    @FXML
    public void filtrar(ActionEvent actionEvent) {
        cargar();
    }

    /**
     * Borra todos los filtros y recarga la tabla con todas las ofertas.
     *
     * @param actionEvent evento de acción generado al pulsar el botón limpiar
     */
    @FXML
    public void limpiar(ActionEvent actionEvent) {
        txtTexto.clear();
        txtEmpresa.clear();
        txtSalarioMin.clear();
        txtSalarioMax.clear();
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        cmbEstado.getSelectionModel().selectFirst();
        cargar();
    }

    /**
     * Abre la pantalla de detalle de la oferta seleccionada.
     *
     * @param actionEvent evento de acción generado al pulsar el botón detalle
     */
    @FXML
    public void verDetalle(ActionEvent actionEvent) {
        Oferta oferta = seleccionada();
        if (oferta != null) {
            AppContext.getInstance().put("oferta", oferta);
            SceneNavigator.navigateTo("oferta-detail-view.fxml", "Detalle de candidatura");
        }
    }

    /**
     * Abre el formulario para crear una oferta nueva.
     *
     * @param actionEvent evento de acción generado al pulsar el botón nueva
     */
    @FXML
    public void nueva(ActionEvent actionEvent) {
        AppContext.getInstance().remove("oferta");
        AppContext.getInstance().put("origen", "ofertas-view.fxml");
        SceneNavigator.navigateTo("oferta-form-view.fxml", "Nueva candidatura");
    }

    /**
     * Abre el formulario para editar la oferta seleccionada.
     *
     * @param actionEvent evento de acción generado al pulsar el botón editar
     */
    @FXML
    public void editar(ActionEvent actionEvent) {
        Oferta oferta = seleccionada();
        if (oferta != null) {
            AppContext.getInstance().put("oferta", oferta);
            AppContext.getInstance().put("origen", "ofertas-view.fxml");
            SceneNavigator.navigateTo("oferta-form-view.fxml", "Editar candidatura");
        }
    }

    /**
     * Borra la oferta seleccionada (y sus entrevistas) tras pedir confirmación.
     *
     * @param actionEvent evento de acción generado al pulsar el botón borrar
     */
    @FXML
    public void borrar(ActionEvent actionEvent) {
        Oferta oferta = seleccionada();
        if (oferta == null) {
            return;
        }
        if (Alertas.confirmar("¿Borrar la candidatura \"" + oferta.getTitulo()
                + "\" y sus entrevistas?")) {
            try {
                ofertaDAO.deleteById(oferta.getId());
                cargar();
            } catch (RuntimeException e) {
                Alertas.error("No se pudo borrar la candidatura: " + e.getMessage());
            }
        }
    }

    /**
     * Navega a la gestión de empresas.
     *
     * @param actionEvent evento de acción generado al pulsar el botón empresas
     */
    @FXML
    public void irEmpresas(ActionEvent actionEvent) {
        SceneNavigator.navigateTo("empresas-view.fxml", "Empresas");
    }

    /**
     * Permite elegir un CSV y lo importa a la base de datos.
     *
     * @param actionEvent evento de acción generado al pulsar el botón importar
     */
    @FXML
    public void importar(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importar candidaturas desde CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ficheros CSV", "*.csv"));
        File fichero = chooser.showOpenDialog(table.getScene().getWindow());
        if (fichero == null) {
            return;
        }

        try {
            int nuevas = importService.importar(fichero.toPath());
            Alertas.info("Importación correcta: " + nuevas + " candidaturas añadidas.");
            cargar();
        } catch (NoSuchFileException e) {
            Alertas.error("Archivo no encontrado: " + fichero);
        } catch (IOException e) {
            Alertas.error("Error al leer el archivo: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Alertas.error("CSV con formato incorrecto. " + e.getMessage());
        } catch (RuntimeException e) {
            Alertas.error("Error al guardar en la base de datos: " + e.getMessage());
        }
    }

    /* Métodos auxiliares */

    /**
     * Consulta las ofertas con los filtros actuales y rellena la tabla.
     */
    private void cargar() {
        try {
            List<Oferta> ofertas = ofertaDAO.buscar(filtroActual());
            table.setItems(FXCollections.observableArrayList(ofertas));
            lblTotal.setText(ofertas.size() + " candidaturas");
        } catch (IllegalArgumentException e) {
            Alertas.error(e.getMessage());
        } catch (RuntimeException e) {
            Alertas.error("Error al consultar la base de datos: " + e.getMessage());
        }
    }

    /**
     * Construye el filtro a partir de los campos del formulario, validándolos.
     *
     * @return el filtro con los criterios introducidos
     * @throws IllegalArgumentException si algún criterio no es válido
     */
    private FiltroOferta filtroActual() {
        String estado = cmbEstado.getValue();
        if (estado == null || TODOS.equals(estado)) {
            estado = null;
        }
        Double min = parseSalario(txtSalarioMin.getText(), "salario mínimo");
        Double max = parseSalario(txtSalarioMax.getText(), "salario máximo");
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException("El salario mínimo no puede ser mayor que el máximo.");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha \"desde\" no puede ser posterior a \"hasta\".");
        }
        return new FiltroOferta(txtTexto.getText(), txtEmpresa.getText(), estado, min, max, desde, hasta);
    }

    /**
     * Convierte el texto de un campo de salario en número.
     *
     * @param texto  texto introducido (admite coma o punto decimal)
     * @param nombre nombre del campo, para el mensaje de error
     * @return el valor numérico, o {@code null} si el campo está vacío
     * @throws IllegalArgumentException si no es un número válido o es negativo
     */
    private Double parseSalario(String texto, String nombre) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        double valor;
        try {
            valor = Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El " + nombre + " debe ser un número.");
        }
        if (valor < 0) {
            throw new IllegalArgumentException("El " + nombre + " no puede ser negativo.");
        }
        return valor;
    }

    /**
     * Devuelve la oferta seleccionada en la tabla o avisa si no hay ninguna.
     *
     * @return la oferta seleccionada, o {@code null} si no hay selección
     */
    private Oferta seleccionada() {
        Oferta oferta = table.getSelectionModel().getSelectedItem();
        if (oferta == null) {
            Alertas.info("Selecciona primero una candidatura de la tabla.");
        }
        return oferta;
    }
}
