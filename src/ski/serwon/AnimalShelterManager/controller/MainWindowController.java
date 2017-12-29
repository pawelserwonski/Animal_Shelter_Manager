package ski.serwon.AnimalShelterManager.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ski.serwon.AnimalShelterManager.model.Animal;
import ski.serwon.AnimalShelterManager.model.Breed;
import ski.serwon.AnimalShelterManager.model.Species;
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



    public void initialize() {

        SortedList<Animal> animalSortedList = new SortedList<Animal>
                (AnimalDatabase.getInstance().getAnimals()
                        , Comparator.comparing(Animal::getName));

        animalsListView.setItems(animalSortedList);
        animalsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        animalsListView.getSelectionModel().selectFirst();


        SortedList<Species> speciesSortedList = new SortedList<Species>
                (SpeciesDatabase.getInstance().getSpeciesList()
                        , Comparator.comparing(Species::getName));

        speciesListView.setItems(speciesSortedList);

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

}
