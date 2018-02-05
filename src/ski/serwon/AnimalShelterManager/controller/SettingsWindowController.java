package ski.serwon.AnimalShelterManager.controller;

import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyEvent;
import ski.serwon.AnimalShelterManager.model.datamodel.ApplicationSettings;


public class SettingsWindowController {
    @FXML
    Spinner<Integer> thresholdWaringSpinner;


    public void saveSettings() {
        ApplicationSettings.setPercentOfFreePlacesToWarning(thresholdWaringSpinner.getValue());
    }

    public void setThresholdWaringSpinner(int value) {
        thresholdWaringSpinner.getValueFactory().setValue(value);
    }

    public Spinner<Integer> getThresholdWaringSpinner() {
        return thresholdWaringSpinner;
    }
}

