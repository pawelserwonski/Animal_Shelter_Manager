package ski.serwon.AnimalShelterManager.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import ski.serwon.AnimalShelterManager.model.Animal;
import ski.serwon.AnimalShelterManager.model.Breed;
import ski.serwon.AnimalShelterManager.model.Species;
import ski.serwon.AnimalShelterManager.model.WalkException;
import ski.serwon.AnimalShelterManager.model.datamodel.AnimalDatabase;
import ski.serwon.AnimalShelterManager.model.datamodel.ApplicationSettings;
import ski.serwon.AnimalShelterManager.model.datamodel.SpeciesDatabase;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainWindowController {
    @FXML
    ComboBox<Species> speciesComboBox;

    @FXML
    ListView<Animal> animalsListView;

    @FXML
    ListView<Species> speciesListView;

    @FXML
    ListView<Breed> breedsListView;

    @FXML
    GridPane mainGridPane;

    @FXML
    Label nameLabel;
    @FXML
    Label idLabel;
    @FXML
    Label sexLabel;
    @FXML
    Label speciesLabel;
    @FXML
    Label breedLabel;
    @FXML
    Label inShelterSinceLabel;
    @FXML
    Label birthdateLabel;
    @FXML
    Label lastWalkedLabel;
    @FXML
    Label occupiedPlacesLabel;
    @FXML
    Label placesLimitLabel;
    @FXML
    Label freePlacesPercentLabel;

    @FXML
    Button addButton;
    @FXML
    Button editButton;
    @FXML
    Button deleteButton;
    @FXML
    Button walkOutButton;

    private SortedList<Animal> animalSortedList = new SortedList<Animal>
            (AnimalDatabase.getInstance().getAnimals(), Comparator.comparing(Animal::getName));


    public void initialize() {
        handleAnimalsListViewInitialize();
        handleSpeciesListViewInitialize();
        handleSpeciesComboBoxInitialize();

        List<Species> closeToLimitSpeciesList = speciesListView.getItems()
                .stream()
                .filter(c -> c.getPercentOfFreePlaces() < ApplicationSettings.percentOfFreePlacesToWarning)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));


        if (closeToLimitSpeciesList.size() > 0) {
            StringBuilder message = new StringBuilder("List of species with a small percentage of vacancies:\n");
            closeToLimitSpeciesList.forEach(c -> message.append(c.getName() + "\n"));
            showWarning("Some species are getting close to the limits", message.toString());
        }
    }

    private void handleAnimalsListViewInitialize() {

        animalsListView.setItems(animalSortedList);
        animalsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        animalsListView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<Animal>() {
                    @Override
                    public void changed(ObservableValue<? extends Animal> observable, Animal oldValue, Animal newValue) {
                        showSelectedAnimal();
                    }
                });

        animalsListView.getSelectionModel().selectFirst();

        animalsListView.setCellFactory(new Callback<ListView<Animal>, ListCell<Animal>>() {
            @Override
            public ListCell<Animal> call(ListView<Animal> param) {
                ListCell<Animal> cell = new ListCell<Animal>() {
                    @Override
                    protected void updateItem(Animal item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item.getName());
                            try {
                                if (item.daysSinceLastWalked() > 7) {
                                    setTextFill(Color.RED);
                                }
                            } catch (WalkException e) {
                            }

                        }
                    }
                };
                return cell;
            }
        });
    }

    private void handleSpeciesListViewInitialize() {
        SortedList<Species> speciesSortedList = new SortedList<Species>
                (SpeciesDatabase.getInstance().getSpeciesList()
                        , Comparator.comparing(Species::getName));

        speciesListView.setItems(speciesSortedList);
        speciesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        speciesListView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<Species>() {
                    @Override
                    public void changed(ObservableValue<? extends Species> observable, Species oldValue, Species newValue) {
                        showBreedsForSelectedSpecies();
                        populateSpeciesInfoLabels(newValue);
                    }
                });
        speciesListView.getSelectionModel().selectFirst();

        speciesListView.setCellFactory(getSpeciesCellFactory());

    }

    private void populateSpeciesInfoLabels(Species species) {
        occupiedPlacesLabel.setText(String.valueOf(species.getOccupiedPlaces()));
        placesLimitLabel.setText(String.valueOf(species.getPlacesLimit()));
        freePlacesPercentLabel.setText(String.valueOf(species.getPercentOfFreePlaces()) + "%");
    }

    private void handleSpeciesComboBoxInitialize() {
        speciesComboBox.setItems(speciesListView.getItems());
        speciesComboBox.setButtonCell(getSpeciesCellFactory().call(null));
        speciesComboBox.setCellFactory(getSpeciesCellFactory());

        speciesComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Species>() {
            @Override
            public void changed(ObservableValue<? extends Species> observable, Species oldValue, Species newValue) {
                if (newValue == null) {
                    animalsListView.setItems(animalSortedList);
                } else {
                    ObservableList<Animal> list = animalSortedList.stream()
                            .filter(c -> newValue.getBreeds().contains(c.getBreed()))
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));

                    animalsListView.setItems(list);
                }
            }
        });
    }

    @FXML
    private void clearSpeciesSelection() {
        speciesComboBox.getSelectionModel().clearSelection();
    }

    private Callback<ListView<Species>, ListCell<Species>> getSpeciesCellFactory() {
        return new Callback<>() {
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
                            if (item.getPercentOfFreePlaces() < ApplicationSettings.percentOfFreePlacesToWarning) {
                                setTextFill(Color.RED);
                            } else {
                                setTextFill(Color.BLACK);
                            }
                        }
                    }
                };
                return cell;
            }
        };
    }

    @FXML
    private void showNewSpeciesDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainGridPane.getScene().getWindow());
        dialog.setTitle("Add species");
        dialog.setHeaderText("Add new species to database");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("ski" + File.separator
                + "serwon" + File.separator + "AnimalShelterManager" + File.separator
                + "view" + File.separator + "addEditSpecies.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("IOException");
            //todo
        }


        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);


        Optional<ButtonType> result = dialog.showAndWait();


        if (result.isPresent() && result.get() == ButtonType.OK) {
            AddSpeciesController controller = fxmlLoader.getController();
            Species newSpecies = controller.addSpecies();
            speciesListView.getSelectionModel().select(newSpecies);
        }
    }

    @FXML
    private void showBreedsForSelectedSpecies() {
        Species selectedSpecies = speciesListView
                .getSelectionModel().getSelectedItem();

        SortedList<Breed> speciesSortedList = new SortedList<>
                (selectedSpecies.getBreeds(), Comparator.comparing(Breed::getName));
        breedsListView.setItems(speciesSortedList);

        breedsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        breedsListView.getSelectionModel().selectFirst();

        breedsListView.setCellFactory(new Callback<ListView<Breed>, ListCell<Breed>>() {
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
        });
    }

    @FXML
    private void showEditSpeciesDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainGridPane.getScene().getWindow());
        dialog.setTitle("Edit limit");
        dialog.setHeaderText("Edit places limit for existing species");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("ski" + File.separator
                + "serwon" + File.separator + "AnimalShelterManager" + File.separator
                + "view" + File.separator + "addEditSpecies.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("IOException");
            //todo
        }

        Species selectedSpecies = speciesListView
                .getSelectionModel().getSelectedItem();


        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        AddSpeciesController controller = fxmlLoader.getController();
        controller.fillFieldsWithSpecies(selectedSpecies);

        Optional<ButtonType> result = dialog.showAndWait();


        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.editSpecies(selectedSpecies);
            speciesListView.refresh();
            populateSpeciesInfoLabels(selectedSpecies);
        }
    }

    @FXML
    private void addNewBreed() {
        Species selectedSpecies = speciesListView
                .getSelectionModel().getSelectedItem();

        if (selectedSpecies == null) {
            showWarning("Warning", "Species must be selected before adding breed to it");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainGridPane.getScene().getWindow());
        dialog.setTitle("Add breed");
        dialog.setHeaderText("Add new breed to database");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("ski" + File.separator
                + "serwon" + File.separator + "AnimalShelterManager" + File.separator
                + "view" + File.separator + "addEditBreed.fxml"));
        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: 06.01.2018
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        AddBreedController controller = loader.getController();

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.addNewBreed(selectedSpecies);
        }
    }

    @FXML
    private void editBreed() {
        Breed selectedBreed = breedsListView.getSelectionModel().getSelectedItem();

        if (selectedBreed == null) {
            showWarning("Warning", "Select breed from the list");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainGridPane.getScene().getWindow());
        dialog.setTitle("Edit breed");
        dialog.setHeaderText("Edit existing breed");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("ski" + File.separator
                + "serwon" + File.separator + "AnimalShelterManager" + File.separator
                + "view" + File.separator + "addEditBreed.fxml"));
        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: 06.01.2018
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        AddBreedController controller = loader.getController();

        controller.fillFieldsWithBreed(selectedBreed);
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.editBreed(selectedBreed);
            animalsListView.refresh();
        }
    }

    @FXML
    private void showNewAnimalDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainGridPane.getScene().getWindow());
        dialog.setTitle("Add animal");
        dialog.setHeaderText("Add new animal to database");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("ski" + File.separator
                + "serwon" + File.separator + "AnimalShelterManager" + File.separator
                + "view" + File.separator + "addEditAnimal.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("IOException");
            //todo
        }

        AddEditAnimalController controller = fxmlLoader.getController();
        controller.populateSpeciesComboBox();

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);


        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.addNewAnimal();
            animalsListView.refresh();
        }

    }

    @FXML
    private void showEditAnimalDialog() {
        Animal selectedAnimal = animalsListView.getSelectionModel().getSelectedItem();
        if (selectedAnimal == null) {
            showWarning("Select animal", "You have to select animal " +
                    "from the list in order to edit it");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainGridPane.getScene().getWindow());
        dialog.setTitle("Edit animal");
        dialog.setHeaderText("Edit animal existing in database");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("ski" + File.separator
                + "serwon" + File.separator + "AnimalShelterManager" + File.separator
                + "view" + File.separator + "addEditAnimal.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("IOException");
            //todo
        }

        AddEditAnimalController controller = fxmlLoader.getController();
        controller.populateSpeciesComboBox();
        controller.fillFieldsWithAnimal(selectedAnimal);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);


        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.editExistingAnimal(selectedAnimal);
            animalsListView.refresh();
        }
    }

    @FXML
    private void walkOut() {
        Animal selectedAnimal = animalsListView.getSelectionModel().getSelectedItem();
        if (selectedAnimal == null) {
            return;
        }

        AnimalDatabase.getInstance().walkOutAnimal(selectedAnimal);
        animalsListView.refresh();
        showSelectedAnimal();
    }

    @FXML
    private void deleteAnimal() {
        Animal selectedAnimal = animalsListView.getSelectionModel().getSelectedItem();
        if (selectedAnimal == null) {
            showWarning("Oops", "You have to select animal in order to delete it");
            return;
        }

        if (!AnimalDatabase.getInstance().deleteAnimal(selectedAnimal)) {
            showError("Error", "Animal couldn't have been removed. Try again");
        }
        animalsListView.refresh();
        animalsListView.getSelectionModel().selectFirst();
    }

    @FXML
    private void closeMenuItemHandler() {
        Platform.exit();
    }

    @FXML
    private void settingsHandler() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainGridPane.getScene().getWindow());
        dialog.setTitle("Settings");

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("ski" + File.separator
                + "serwon" + File.separator + "AnimalShelterManager" + File.separator
                + "view" + File.separator + "settingsWindow.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("IOException");
            showError("Internal problem", "Problem with loading window occurred.");
        }

        SettingsWindowController controller = fxmlLoader.getController();
        controller.setThresholdWaringSpinner(ApplicationSettings.getPercentOfFreePlacesToWarning());



        ButtonType saveButtonType = new ButtonType("Save");
        dialog.getDialogPane().getButtonTypes().add(saveButtonType);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButtonType) {
            controller.saveSettings();
            speciesListView.refresh();
        }
    }

    @FXML
    private void aboutMenuItemHandler() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About the program");

        FlowPane flowPane = new FlowPane();
        flowPane.setPrefWrapLength(280);

        Label desc = new Label("Animal Shelter Manager      Created by Paweł Serwoński");
        Label git = new Label("Github - ");

        Hyperlink hyperlink = new Hyperlink("https://github.com/pawelserwonski/");
        hyperlink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Desktop.getDesktop().browse(URI.create(hyperlink.getText()));
                } catch (IOException e) {
                    showError("Error", "Error during opening default browser");
                }
            }
        });

        flowPane.getChildren().addAll(desc, git, hyperlink);
        alert.getDialogPane().contentProperty().set(flowPane);
        alert.show();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSelectedAnimal() {
        Animal selectedAnimal = animalsListView.getSelectionModel().getSelectedItem();

        nameLabel.setText(selectedAnimal.getName());
        idLabel.setText(String.valueOf(selectedAnimal.getId()));
        sexLabel.setText(selectedAnimal.getSex().getFullNameOfSex());
        breedLabel.setText(selectedAnimal.getBreed().getName());
        inShelterSinceLabel.setText(selectedAnimal.getInShelterSince().toString());
        birthdateLabel.setText(selectedAnimal.getBirthDate().toString());
        try {
            lastWalkedLabel.setText(String.valueOf(selectedAnimal.daysSinceLastWalked())
                    + " - " + selectedAnimal.getLastWalk());
            walkOutButton.setVisible(true);
        } catch (WalkException e) {
            lastWalkedLabel.setText("Animal does not need to be walked out.");
            walkOutButton.setVisible(false);
        }
    }


}

