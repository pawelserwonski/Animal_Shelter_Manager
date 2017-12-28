package ski.serwon.AnimalShelterManager.model.datamodel;

import ski.serwon.AnimalShelterManager.model.Breed;

import java.util.List;

public class BreedDatabase {
    private static BreedDatabase instance = new BreedDatabase();
    private List<Breed> breeds;

    public static BreedDatabase getInstance() {
        return instance;
    }

    public static boolean addBreed(String name, boolean requiresWalk) {
        if (instance.breeds.stream()
                .map(Breed::getName)
                .anyMatch(c -> c.equals(name))) {
            return false;
        }

        instance.breeds.add(new Breed(name, requiresWalk));
        return true;
        //todo -- add new breed to database
    }

    public Breed getBreedViaId(int id) {
        //TODO
    }
}
