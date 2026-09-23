package com.example.intranet_adm.view.components;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/** Ícones vetoriais da interface. Mantém os símbolos nítidos em qualquer escala. */
public final class AppIcon {

    public enum Type {
        BELL("M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.1-1.6-5.6-4.5-6.3V4c0-.8-.7-1.5-1.5-1.5S10.5 3.2 10.5 4v.7C7.6 5.3 6 7.9 6 11v5l-2 2v1h16v-1l-2-2z"),
        PLUS("M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"),
        POPUP("M20 2H4C2.9 2 2 2.9 2 4v14c0 1.1.9 2 2 2h4l4 3 4-3h4c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-4H6V8h12v2z"),
        HISTORY("M13 3a9 9 0 1 1-8.5 6H2l3.5-3.5L9 9H6.6A7 7 0 1 0 13 5v4l3.5 2.1-1 1.7L11 10V3h2z"),
        USERS("M16 11c1.7 0 3-1.3 3-3s-1.3-3-3-3-3 1.3-3 3 1.3 3 3 3zm-8 0c1.7 0 3-1.3 3-3S9.7 5 8 5 5 6.3 5 8s1.3 3 3 3zm0 2c-2.3 0-7 1.2-7 3.5V19h14v-2.5C15 14.2 10.3 13 8 13zm8 0c-.3 0-.7 0-1.1.1 1.2.9 2.1 2 2.1 3.4V19h6v-2.5c0-2.3-4.7-3.5-7-3.5z"),
        MESSAGE("M20 2H4C2.9 2 2 2.9 2 4v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 7h12v2H6V7zm8 7H6v-2h8v2z"),
        SETTINGS("M19.4 13a7.7 7.7 0 0 0 .1-1 7.7 7.7 0 0 0-.1-1l2.1-1.6-2-3.4-2.5 1a7.5 7.5 0 0 0-1.7-1L15 3.5h-4L10.6 6a7.5 7.5 0 0 0-1.7 1L6.5 6l-2 3.4L6.6 11a7.7 7.7 0 0 0-.1 1 7.7 7.7 0 0 0 .1 1l-2.1 1.6 2 3.4 2.4-1a7.5 7.5 0 0 0 1.7 1l.4 2.5h4l.4-2.5a7.5 7.5 0 0 0 1.7-1l2.4 1 2-3.4L19.4 13zM13 15.5a3.5 3.5 0 1 1 0-7 3.5 3.5 0 0 1 0 7z"),
        LOGOUT("M10 17l1.4-1.4L8.8 13H20v-2H8.8l2.6-2.6L10 7l-5 5 5 5zm-6 4h8v-2H4V5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2z"),
        INFO("M11 17h2v-6h-2v6zm1-15a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm0 18a8 8 0 1 1 0-16 8 8 0 0 1 0 16zm-1-11h2V7h-2v2z"),
        REFRESH("M17.7 6.3A8 8 0 1 0 20 12h-2a6 6 0 1 1-1.8-4.2L13 11h8V3l-3.3 3.3z"),
        EYE("M12 4.5C7 4.5 2.7 7.6 1 12c1.7 4.4 6 7.5 11 7.5s9.3-3.1 11-7.5C21.3 7.6 17 4.5 12 4.5zm0 12.5a5 5 0 1 1 0-10 5 5 0 0 1 0 10zm0-2a3 3 0 1 0 0-6 3 3 0 0 0 0 6z"),
        SEND("M2 21l21-9L2 3v7l15 2-15 2v7z"),
        TRASH("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4z"),
        EDIT("M3 17.3V21h3.7L17.8 9.9l-3.7-3.7L3 17.3zM20.7 7c.4-.4.4-1 0-1.4l-2.3-2.3a1 1 0 0 0-1.4 0l-1.8 1.8 3.7 3.7L20.7 7z"),
        LINK("M3.9 12c0-1.7 1.4-3.1 3.1-3.1h4V7H7a5 5 0 0 0 0 10h4v-1.9H7A3.1 3.1 0 0 1 3.9 12zM8 13h8v-2H8v2zm9-6h-4v1.9h4a3.1 3.1 0 1 1 0 6.2h-4V17h4a5 5 0 0 0 0-10z"),
        IMAGE("M21 19V5c0-1.1-.9-2-2-2H5C3.9 3 3 3.9 3 5v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 11.5l2.5 3 3.5-4.5 4.5 6H5l3.5-4.5z"),
        SERVER("M4 2h16c1.1 0 2 .9 2 2v4c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2zm0 12h16c1.1 0 2 .9 2 2v4c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2v-4c0-1.1.9-2 2-2zm2-8v2h2V6H6zm0 12v2h2v-2H6z"),
        SAVE("M17 3H5C3.9 3 3 3.9 3 5v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zM6 5h9v4H6V5zm6 14a4 4 0 1 1 0-8 4 4 0 0 1 0 8z"),
        SEARCH("M9.5 3a6.5 6.5 0 1 0 4.1 11.5L19 20l1-1-5.5-5.4A6.5 6.5 0 0 0 9.5 3zm0 2a4.5 4.5 0 1 1 0 9 4.5 4.5 0 0 1 0-9z"),
        CLOSE("M18.3 5.7 12 12l-6.3-6.3-1.4 1.4 6.3 6.3-6.3 6.3 1.4 1.4 6.3-6.3 6.3 6.3 1.4-1.4-6.3-6.3 6.3-6.3z");

        private final String path;

        Type(String path) {
            this.path = path;
        }
    }

    private AppIcon() {
    }

    public static StackPane create(Type type, double size) {
        SVGPath shape = new SVGPath();
        shape.setContent(type.path);
        shape.getStyleClass().add("app-icon-shape");
        double scale = size / 24.0;
        shape.setScaleX(scale);
        shape.setScaleY(scale);

        StackPane wrapper = new StackPane(shape);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);
        wrapper.setMaxSize(size, size);
        wrapper.getStyleClass().add("app-icon");
        return wrapper;
    }
}
