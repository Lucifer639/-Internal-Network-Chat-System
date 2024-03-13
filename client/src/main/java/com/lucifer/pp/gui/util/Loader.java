package com.lucifer.pp.gui.util;

import javafx.fxml.FXMLLoader;

import java.io.InputStream;

public class Loader {
    public static <T> T load(String fxml, Class<T> clazz) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        InputStream inputStream = Loader.class.getClassLoader().getResourceAsStream(fxml);
        return clazz.cast(fxmlLoader.load(inputStream));
    }

    public static <T> T getController(String fxml) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        InputStream inputStream = Loader.class.getClassLoader().getResourceAsStream(fxml);
        fxmlLoader.load(inputStream);
        return fxmlLoader.getController();
    }


}
