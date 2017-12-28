package ski.serwon.AnimalShelterManager.model;

import java.util.List;

public class Species {
    public static List<Species> speciesList; //TODO - create species list in database and retrieve it

    private String name;
    private int placesLimit;
    private int id;
    private List<Breed> breeds;

    public boolean addBreed(String name, boolean requireWalks) {
        return Breed.addBreed(breeds, name, requireWalks);
    }

    public Species(String name, int placesLimit) {
        this.name = name;
        this.placesLimit = placesLimit;
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
}
