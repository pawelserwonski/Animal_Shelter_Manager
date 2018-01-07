package ski.serwon.AnimalShelterManager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import ski.serwon.AnimalShelterManager.model.Animal;
import ski.serwon.AnimalShelterManager.model.Breed;
import ski.serwon.AnimalShelterManager.model.Species;
import ski.serwon.AnimalShelterManager.model.datamodel.AnimalDatabase;
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

    public void addNewAnimal() {
        if (!femaleRadioButton.isSelected() && !maleRadioButton.isSelected()) {
            showWarning("Select sex", "Sex needs to be selected");
            return;
        }

        if (nameTextField.getText().equals("")) {
            showWarning("Empty name", "Name cannot be empty");
            return;
        }

        if (breedComboBox.getValue() == null) {
            showWarning("Select breed", "Breed selection cannot be empty");
            return;
        }

        if (birthDatePicker.getValue() == null) {
            showWarning("Select birth date", "Birth date needs to be selected." +
                    " If you don't have exact date, select first day of month, first month of year, etc.");
            return;
        }

        if (!AnimalDatabase.getInstance().addAnimal(
                maleRadioButton.isSelected() ? Animal.Sex.male : Animal.Sex.female,
                nameTextField.getText(), birthDatePicker.getValue(),
                breedComboBox.getValue())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Animal couldn't be added");
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
