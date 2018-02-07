package ski.serwon.AnimalShelterManager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import ski.serwon.AnimalShelterManager.model.Animal;
import ski.serwon.AnimalShelterManager.model.Breed;
import ski.serwon.AnimalShelterManager.model.NoEmptySpacesException;
import ski.serwon.AnimalShelterManager.model.Species;
import ski.serwon.AnimalShelterManager.model.datamodel.AnimalDatabase;
import ski.serwon.AnimalShelterManager.model.datamodel.BreedDatabase;
import ski.serwon.AnimalShelterManager.model.datamodel.SpeciesDatabase;

public class AddEditAnimalController {
    @FXML
    private ComboBox<Species> speciesComboBox;
    @FXML
    private ComboBox<Breed> breedComboBox;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private TextField nameTextField;
    @FXML
    private RadioButton femaleRadioButton;
    @FXML
    private RadioButton maleRadioButton;

    /**
     * Populates combo box with all species from {@link SpeciesDatabase}
     */
    public void populateSpeciesComboBox() {
        speciesComboBox.setItems(SpeciesDatabase.getInstance().getSpeciesList());
        Callback cellFactory = new Callback<ListView<Species>, ListCell<Species>>() {
            @Override
            public ListCell<Species> call(ListView<Species> param) {
                ListCell<Species> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(Species item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
                return cell;
            }
        };


        speciesComboBox.setButtonCell((ListCell<Species>) cellFactory.call(null));
        speciesComboBox.setCellFactory(cellFactory);
    }

    /**
     * Populates combo box with breeds basing on selected {@link Species} object.
     */
    @FXML
    private void populateBreedComboBox() {
        breedComboBox.setItems(speciesComboBox.getValue().getBreeds());
        Callback cellFactory = new Callback<ListView<Breed>, ListCell<Breed>>() {
            @Override
            public ListCell<Breed> call(ListView<Breed> param) {
                ListCell<Breed> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(Breed item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
                return cell;
            }
        };

        breedComboBox.setButtonCell((ListCell<Breed>) cellFactory.call(null));
        breedComboBox.setCellFactory(cellFactory);
    }

    /**
     * Fills fields in controlled window with values stored in passed {@link Animal} object.
     * @param  animal Object to fill window with
     */
    public void fillFieldsWithAnimal(Animal animal) {
        int speciesId = BreedDatabase.getInstance().getSpeciesIdForBreed(animal.getBreed());
        if (speciesId < 0) {
            String warningContent = "";
            switch (speciesId) {
                case -1:
                    warningContent = "Internal error - breed not selected";
                    break;
                case -2:
                    warningContent = "Problem with database occurred";
                    break;
                case -3:
                    warningContent = "No breed with such id in database";
                    break;
            }
            showWarning("Oops", warningContent);
            return;
        }

        Species species = SpeciesDatabase.getInstance().getSpeciesById(speciesId);
        speciesComboBox.getSelectionModel().select(species);
        speciesComboBox.setDisable(true);
        populateBreedComboBox();
        breedComboBox.getSelectionModel().select(animal.getBreed());
        birthDatePicker.setValue(animal.getBirthDate());
        nameTextField.setText(animal.getName());
        if (animal.getSex() == Animal.Sex.male) {
            maleRadioButton.setSelected(true);
        } else {
            femaleRadioButton.setSelected(true);
        }
    }

    /**
     * Handles setting new values in existing {@link Animal}
     * object basing on values passed by user in controlled window.
     *
     * @param animal Edited animal
     */
    public void editExistingAnimal(Animal animal) {
        if (!checkFields()) {
            return;
        }

        if (!AnimalDatabase.getInstance().editAnimal(animal,
                breedComboBox.getValue(), nameTextField.getText(),
                maleRadioButton.isSelected() ? Animal.Sex.male : Animal.Sex.female,
                birthDatePicker.getValue())) {
            showError("Error", "Problem with editing occurred");
        }

    }

    /**
     * Handles adding new {@link Animal} object basing on
     * values passed by user in controlled window.
     *
     * Shows warning if problems occurred.
     */
    public void addNewAnimal() {
        if (!checkFields()) {
            return;
        }

        try {
            if (!AnimalDatabase.getInstance().addAnimal(
                    maleRadioButton.isSelected() ? Animal.Sex.male : Animal.Sex.female,
                    nameTextField.getText(), birthDatePicker.getValue(),
                    breedComboBox.getValue())) {
                showError("Error", "Animal couldn't be added");
            }
        } catch (NoEmptySpacesException e) {
            showError("All places taken", e.getMessage());
        }

    }

    /**
     * Shows {@link Alert.AlertType#ERROR} alert
     *
     * @param title Title of new alert
     * @param content Content of new alert
     */

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
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

    /**
     * Checks if every field is filled.
     *
     * @return true if all fields are filled; false otherwise
     */
    private boolean checkFields() {
        if (!femaleRadioButton.isSelected() && !maleRadioButton.isSelected()) {
            showWarning("Select sex", "Sex needs to be selected");
            return false;
        }

        if (nameTextField.getText().equals("")) {
            showWarning("Empty name", "Name cannot be empty");
            return false;
        }

        if (breedComboBox.getValue() == null) {
            showWarning("Select breed", "Breed selection cannot be empty");
            return false;
        }

        if (birthDatePicker.getValue() == null) {
            showWarning("Select birth date", "Birth date needs to be selected." +
                    " If you don't have exact date, select first day of month, first month of year, etc.");
            return false;
        }
        return true;
    }
}
