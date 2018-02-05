package ski.serwon.AnimalShelterManager.model;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Class represents single animal in shelter.
 *
 * @version 1.0
 * @since 1.0
 */
public class Animal {

    /**
     * Enumerator representing sex of certain animal.
     */
    public enum Sex{male, female;

        @Override
        public String toString() {
            return this.equals(male) ? "m" : "f";
        }

        public static Sex getSexFromString(String sex) {
            return sex.toLowerCase().equals("m") ? male : female;
        }

        public String getFullNameOfSex() { return this.equals(male) ? "Male" : "Female";}
    }

    /**
     * Animal's {@link Sex}
     */
    private Sex sex;

    /**
     * Animal's name
     */
    private String name;

    /**
     * Animal's date of birth.
     * @see LocalDate
     */
    private LocalDate birthDate;

    /**
     * Date since when the animal is in the shelter.
     * @see LocalDate
     */
    private LocalDate inShelterSince;

    /**
     * Animal's {@link Breed}
     */
    private Breed breed;

    /**
     * Date when animal was walked out for the last time
     * (only if {@link Breed#requireWalk} == true)
     */
    private LocalDate lastWalk;

    /**
     * Animal's ID in database
     */
    private int id;

    /**
     * Class constructor.
     * @param sex Animal's {@link Sex}
     * @param name Animal's name
     * @param birthDate Animal's date of birth.
     * @param inShelterSince Date since when the animal is in the shelter.
     * @param breed Animal's {@link Breed}
     * @param lastWalk Date when animal was walked out for the last time
     *                 (necessary only if {@link Breed#requireWalk} == true)
     * @param id Animal's ID in database
     */
    public Animal(Sex sex, String name, LocalDate birthDate, LocalDate inShelterSince,
                  Breed breed, LocalDate lastWalk, int id) {
        this.sex = sex;
        this.name = name;
        this.birthDate = birthDate;
        this.inShelterSince = inShelterSince;
        this.breed = breed;
        this.lastWalk = lastWalk;
        this.id = id;
    }

    /**
     * Method count how many days have passed since {@link #lastWalk}.
     * @return Days since {@link #lastWalk}
     * @throws WalkException if {@link Breed#requireWalk} == false
     */
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

    public void setLastWalk(LocalDate lastWalk) {
        this.lastWalk = lastWalk;
    }

    public int getId() {
        return id;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setBreed(Breed breed) {
        this.breed = breed;
    }
}
