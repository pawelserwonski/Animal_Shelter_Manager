package ski.serwon.AnimalShelterManager.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ski.serwon.AnimalShelterManager.model.datamodel.BreedDatabase;

import java.util.List;

/***
 * Species represents single 'type' of animal i.e. cat, dog, etc.
 * Every species has its own name, id and list of {@link Breed}
 * which belong to species. Species also have limit of places meaning
 * number of {@link Animal} of certain species which can stay in the shelter at the same time.
 *
 * @version 1.0
 * @since 1.0
 * @see ski.serwon.AnimalShelterManager.model.datamodel.AnimalDatabase
 */

public class Species {

    /**
     * Name of certain species.
     */
    private String name;

    /**
     * Number of {@link Animal} which can stay in the shelter at the same time.
     */
    private int placesLimit;

    /**
     * Species id in database.
     */
    private int id;

    /**
     * List of breeds which belong to the species.
     */
    private ObservableList<Breed> breeds;

    /**
     * Number of {@link Animal} being in shelter at the moment.
     */
    private int occupiedPlaces;


    /**
     * Method creates new {@link Breed} and assign it to the species.
     * @param name Name of new breed.
     * @param requireWalks Does {@link Animal} of new Breed require to be walked out.
     * @return true if {@link Breed} added successful, false otherwise
     * @see BreedDatabase#addBreedToSpecies(Species, String, boolean)
     */
    public boolean addBreed(String name, boolean requireWalks) {
        return BreedDatabase.getInstance().addBreedToSpecies(this, name, requireWalks);
    }

    /**
     * Class constructor. Should be used when adding new species, which was not in database.
     * @param id ID in database
     * @param name Name of new species.
     * @param placesLimit Places limit for new species.
     */
    public Species(int id, String name, int placesLimit) {
        this.id = id;
        this.name = name;
        this.placesLimit = placesLimit;
        this.breeds = FXCollections.observableArrayList
                (BreedDatabase.getInstance().getBreedsOfSpecifiedSpecies(this));
        this.occupiedPlaces = 0;
    }

    /**
     * Class constructor. Should be used to load species from database.
     * @param id ID in database
     * @param name Name of species.
     * @param placesLimit Places limit for species.
     * @param occupiedPlaces Number of {@link Animal} of the species being in shelter at the moment.
     */
    public Species(int id, String name, int placesLimit, int occupiedPlaces) {
        this.id = id;
        this.name = name;
        this.placesLimit = placesLimit;
        this.breeds = FXCollections.observableArrayList
                (BreedDatabase.getInstance().getBreedsOfSpecifiedSpecies(this));
        this.occupiedPlaces = occupiedPlaces;
    }

    /**
     * Method count percent of free places for the species.
     * @return percentage of free places
     */
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
