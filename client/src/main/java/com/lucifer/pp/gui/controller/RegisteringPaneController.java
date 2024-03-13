package com.lucifer.pp.gui.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import lombok.Data;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
@Data
public class RegisteringPaneController implements Initializable {

    @FXML
    private BorderPane registeringPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
