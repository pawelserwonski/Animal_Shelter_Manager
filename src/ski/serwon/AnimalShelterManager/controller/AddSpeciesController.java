package ski.serwon.AnimalShelterManager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import ski.serwon.AnimalShelterManager.model.Species;
import ski.serwon.AnimalShelterManager.model.datamodel.SpeciesDatabase;


public class AddSpeciesController {
    @FXML
    private TextField nameTextField;

    @FXML
    private Spinner<Integer> placesLimitSpinner;

    public Species createSpecies() {
        String name = nameTextField.getText();
        if (name.equals("")) {
            return null;
        }

        int limit = placesLimitSpinner.getValue();

        return SpeciesDatabase.getInstance().addNewSpecies(name, limit);
    }
}
