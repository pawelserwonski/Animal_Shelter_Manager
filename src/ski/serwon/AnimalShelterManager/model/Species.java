package ski.serwon.AnimalShelterManager.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ski.serwon.AnimalShelterManager.model.datamodel.BreedDatabase;

import java.util.List;

public class Species {
    private String name;
    private int placesLimit;
    private int id;
    private ObservableList<Breed> breeds;

    public boolean addBreed(String name, boolean requireWalks) {
        return BreedDatabase.getInstance().addBreedToSpecies(this, name, requireWalks);
    }

    public Species(int id, String name, int placesLimit) {
        this.id = id;
        this.name = name;
        this.placesLimit = placesLimit;
        this.breeds = FXCollections.observableArrayList
                (BreedDatabase.getInstance().getBreedsOfSpecifiedSpecies(this));
    }

    public String getName() {
        return name;
    }

    public int getPlacesLimit() {
        return placesLimit;
    }

    public int getId() {
        return id;
    }

    public ObservableList<Breed> getBreeds() {
        return breeds;
    }

    public void setPlacesLimit(int placesLimit) {
        this.placesLimit = placesLimit;
    }
}
