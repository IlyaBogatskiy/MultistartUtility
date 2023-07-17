module com.ibdev.desktop.application {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.prefs;
    requires java.sql;

    opens com.ibdev.desktop.application to javafx.fxml;
    exports com.ibdev.desktop.application;
    exports com.ibdev.desktop.application.hello;
    opens com.ibdev.desktop.application.hello to javafx.fxml;
}