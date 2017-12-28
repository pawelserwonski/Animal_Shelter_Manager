package ski.serwon.AnimalShelterManager.model;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

public class Animal {
    public enum Sex{male, female}

    private Sex sex;
    //private Species species;
    private String name;
    private LocalDate birthDate;
    private LocalDate inShelterSince;
    private Breed breed;
    private LocalDate lastWalk;
    private int id;



    public Animal(/*Species species, */String name, LocalDate birthDate, Breed breed, Sex sex) {
//        this.species = species;
        this.name = name;
        this.birthDate = birthDate;
        this.breed = breed;
        this.sex = sex;

        this.inShelterSince = LocalDate.now();
        if (breed.doesRequireWalk()) lastWalk = LocalDate.now();
        //id = lastId++;
    }

    public Animal(Sex sex, /*Species species,*/ String name, LocalDate birthDate, LocalDate inShelterSince, Breed breed, LocalDate lastWalk, int id) {
        this.sex = sex;
//        this.species = species;
        this.name = name;
        this.birthDate = birthDate;
        this.inShelterSince = inShelterSince;
        this.breed = breed;
        this.lastWalk = lastWalk;
        this.id = id;
    }

    public long daysSinceLastWalked() throws WalkException {
        if (breed.doesRequireWalk()) {
            return DAYS.between(lastWalk, LocalDate.now());
        } else {
            throw new WalkException("This breed does not require walks.");
        }
    }

    public Sex getSex() {
        return sex;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public LocalDate getInShelterSince() {
        return inShelterSince;
    }

    public Breed getBreed() {
        return breed;
    }

    public LocalDate getLastWalk() {
        return lastWalk;
    }

    public int getId() {
        return id;
    }
}
