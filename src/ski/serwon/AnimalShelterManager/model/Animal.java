package ski.serwon.AnimalShelterManager.model;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

public class Animal {
    private Species species;
    private String name;
    private LocalDate birthDate;
    private LocalDate inShelterSince;
    private Breed breed;
    private LocalDate lastWalk;
    private int id;

    //private static int lastId -- TODO retrieve last id from database method


    public Animal(Species species, String name, LocalDate birthDate, Breed breed) {
        this.species = species;
        this.name = name;
        this.birthDate = birthDate;
        this.breed = breed;


        this.inShelterSince = LocalDate.now();
        if (breed.doesRequireWalk()) lastWalk = LocalDate.now();
        //id = lastId++;
    }


    public long daysSinceLastWalked() throws WalkException {
        if (breed.doesRequireWalk()) {
            return DAYS.between(lastWalk, LocalDate.now());
        } else {
            throw new WalkException("This breed does not require walks.");
        }
    }


}
