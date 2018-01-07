package ski.serwon.AnimalShelterManager.controller;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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


    public void fillFieldsWithSpecies(Species species) {
        nameTextField.setText(species.getName());
        nameTextField.setEditable(false);

        placesLimitSpinner.getValueFactory().setValue(species.getPlacesLimit());
    }

    public void editPlacesLimit(Species species) {

        boolean success = SpeciesDatabase.getInstance()
                .changeSpeciesPlacesLimits
                        (species.getId(), placesLimitSpinner.getValue());

        if (success) {
            species.setPlacesLimit(placesLimitSpinner.getValue());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("SUCCESS");
            alert.setContentText("Limit changes successfully");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Oops...");
            alert.setContentText("There is a problem with database" +
                    ", value couldn't be changed.");
            alert.showAndWait();
        }
    }
}
