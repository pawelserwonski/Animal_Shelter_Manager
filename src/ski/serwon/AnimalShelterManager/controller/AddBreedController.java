package ski.serwon.AnimalShelterManager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import ski.serwon.AnimalShelterManager.model.Breed;
import ski.serwon.AnimalShelterManager.model.Species;
import ski.serwon.AnimalShelterManager.model.datamodel.BreedDatabase;

public class AddBreedController {
    @FXML
    private TextField nameTextField;

    @FXML
    private RadioButton noRadioButton;
    @FXML
    private RadioButton yesRadioButton;


    public void addNewBreed(Species species) {
        if (!noRadioButton.isSelected() && !yesRadioButton.isSelected()) {
            showWarning("Select option", "Select whether this " +
                    "breed requires to be walked.");
            return;
        }

        if (nameTextField.getText().equals("")) {
            showWarning("Empty name", "Name field requires" +
                    "to be filled up");
            return;
        }

        boolean success = species.addBreed(nameTextField.getText(),
                yesRadioButton.isSelected());

        if (!success) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Oops");
            alert.setContentText("Problem with adding breed occurred");
            alert.showAndWait();
        }
    }

    public void fillFieldsWithBreed(Breed breed) {
        nameTextField.setText(breed.getName());
        nameTextField.setEditable(false);
        if (breed.doesRequireWalk()) {
            yesRadioButton.setSelected(true);
        } else {
            noRadioButton.setSelected(true);
        }
    }

    public void editBreed(Breed breed) {
        if (!noRadioButton.isSelected() && !yesRadioButton.isSelected()) {
            showWarning("Select option", "Select whether this " +
                    "breed requires to be walked.");
            return;
        }

        if (!BreedDatabase.getInstance().editBreed(breed,
                yesRadioButton.isSelected())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Oops");
            alert.setContentText("Problem with editing database occurred");
            alert.showAndWait();
        }

    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
