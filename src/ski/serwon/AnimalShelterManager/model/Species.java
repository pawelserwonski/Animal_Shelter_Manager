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
    private int occupiedPlaces;


    public boolean addBreed(String name, boolean requireWalks) {
        return BreedDatabase.getInstance().addBreedToSpecies(this, name, requireWalks);
    }

    public Species(int id, String name, int placesLimit) {
        this.id = id;
        this.name = name;
        this.placesLimit = placesLimit;
        this.breeds = FXCollections.observableArrayList
                (BreedDatabase.getInstance().getBreedsOfSpecifiedSpecies(this));
        this.occupiedPlaces = 0;
    }

    public Species(int id, String name, int placesLimit, int occupiedPlaces) {
        this.id = id;
        this.name = name;
        this.placesLimit = placesLimit;
        this.breeds = FXCollections.observableArrayList
                (BreedDatabase.getInstance().getBreedsOfSpecifiedSpecies(this));
        this.occupiedPlaces = occupiedPlaces;
    }

    public double getPercentOfFreePlaces() {
        return 100.0 - (100 * occupiedPlaces / placesLimit);
    }


    public int getOccupiedPlaces() {
        return occupiedPlaces;
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
