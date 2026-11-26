module org.example.ejerciciointegrador {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires java.naming;
    requires static lombok;

    opens org.example.ejerciciointegrador to javafx.fxml;
    opens org.example.ejerciciointegrador.controllers to javafx.fxml;
    exports org.example.ejerciciointegrador;
}