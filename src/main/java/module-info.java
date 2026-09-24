module com.example.intranet_adm {
    requires javafx.controls;
    requires java.net.http;
    requires java.prefs;
    requires java.desktop;
    requires MaterialFX;

    opens com.example.intranet_adm.model to javafx.base;

    exports com.example.intranet_adm;
    exports com.example.intranet_adm.model;
    exports com.example.intranet_adm.service;
}
