package ski.serwon.AnimalShelterManager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import ski.serwon.AnimalShelterManager.model.Animal;
import ski.serwon.AnimalShelterManager.model.Breed;
import ski.serwon.AnimalShelterManager.model.Species;
import ski.serwon.AnimalShelterManager.model.datamodel.AnimalDatabase;
import ski.serwon.AnimalShelterManager.model.datamodel.BreedDatabase;
import ski.serwon.AnimalShelterManager.model.datamodel.SpeciesDatabase;

import java.time.LocalDate;

public class AddBreedController {
    @FXML
    private TextField nameTextField;
    @FXML
    private RadioButton noRadioButton;
    @FXML
    private RadioButton yesRadioButton;

    /**
     * Handles adding new {@link Breed} object basing on
     * values passed by user in controlled window.
     *
     * Shows warning if problems occurred.
     *
     * @param species {@link Species} object to whom new Breed should be assigned
     */
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

    /**
     * Fills fields in controlled window with values stored in passed {@link Breed} object.
     *1
     * @param breed Object to fill window with
     */
    public void fillFieldsWithBreed(Breed breed) {
        nameTextField.setText(breed.getName());
        nameTextField.setEditable(false);
        if (breed.doesRequireWalk()) {
            yesRadioButton.setSelected(true);
        } else {
            noRadioButton.setSelected(true);
        }
    }

    /**
     * Handles setting new values in existing {@link Breed}
     * object basing on values passed by user in controlled window.
     *
     * @param breed Edited breed
     */
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
            return;
        }

        if (breed.doesRequireWalk()) {
            for (Animal animal : AnimalDatabase.getAnimalsOfSpecifiedBreed(breed)) {
                if (animal.getLastWalk() == null) {
                    AnimalDatabase.getInstance().walkOutAnimal(animal);
                }
            }
        }
    }

    /**
     * Shows {@link Alert.AlertType#WARNING} alert
     *
     * @param title Title of new alert
     * @param content Content of new alert
     */
    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
