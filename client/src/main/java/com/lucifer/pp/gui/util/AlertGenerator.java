package com.lucifer.pp.gui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertGenerator {

    public static Optional<ButtonType> showError(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.setHeaderText(null);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showInfo(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.setHeaderText(null);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showConfirm(String message){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(message);
        alert.setHeaderText(null);
        return alert.showAndWait();
    }

}
