package ski.serwon.AnimalShelterManager.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import ski.serwon.AnimalShelterManager.model.Animal;
import ski.serwon.AnimalShelterManager.model.Breed;
import ski.serwon.AnimalShelterManager.model.Species;
import ski.serwon.AnimalShelterManager.model.WalkException;
import ski.serwon.AnimalShelterManager.model.datamodel.AnimalDatabase;
import ski.serwon.AnimalShelterManager.model.datamodel.SpeciesDatabase;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;

public class MainWindowController {
    @FXML
    ChoiceBox<Species> speciesChoiceBox;

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
    Button addButton;
    @FXML
    Button editButton;
    @FXML
    Button deleteButton;
    @FXML
    Button walkOutButton;

    public void initialize() {
        handleAnimalsListViewInitialize();
        handleSpeciesListViewInitialize();

    }

    private void handleAnimalsListViewInitialize() {
        SortedList<Animal> animalSortedList = new SortedList<Animal>
                (AnimalDatabase.getInstance().getAnimals()
                        , Comparator.comparing(Animal::getName));

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
                    }
                });
        speciesListView.getSelectionModel().selectFirst();

        speciesListView.setCellFactory(new Callback<ListView<Species>, ListCell<Species>>() {
            @Override
            public ListCell<Species> call(ListView<Species> param) {
                ListCell<Species> cell = new ListCell<Species>() {
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
        });

    }

    @FXML
    public void showNewSpeciesDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainGridPane.getScene().getWindow());
        dialog.setTitle("Add species");
        dialog.setHeaderText("Add new species to database");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("ski" + File.separator
                + "serwon" + File.separator + "AnimalShelterManager" + File.separator
                + "view" + File.separator + "addSpecies.fxml"));
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
            Species newSpecies = controller.createSpecies();
            speciesListView.getSelectionModel().select(newSpecies);
        }
    }

    @FXML
    public void showBreedsForSelectedSpecies() {
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
    public void showEditSpeciesDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainGridPane.getScene().getWindow());
        dialog.setTitle("Edit limit");
        dialog.setHeaderText("Edit places limit for existing species");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("ski" + File.separator
                + "serwon" + File.separator + "AnimalShelterManager" + File.separator
                + "view" + File.separator + "addSpecies.fxml"));
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
            controller.editPlacesLimit(selectedSpecies);
        }
    }

    @FXML
    public void addNewBreed() {
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
                + "view" + File.separator + "addBreed.fxml"));
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
    public void editBreed() {
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
                + "view" + File.separator + "addBreed.fxml"));
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
        }
    }

    public void showNewAnimalDialog() {
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
        }

    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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
            lastWalkedLabel.setText(String.valueOf(selectedAnimal.daysSinceLastWalked()));
            walkOutButton.setVisible(true);
        } catch (WalkException e) {
            lastWalkedLabel.setText("Animal does not need to be walked out.");
            walkOutButton.setVisible(false);
        }
    }
}

