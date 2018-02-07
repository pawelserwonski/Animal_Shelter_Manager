package ski.serwon.AnimalShelterManager.model.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ski.serwon.AnimalShelterManager.model.Animal;
import ski.serwon.AnimalShelterManager.model.Breed;
import ski.serwon.AnimalShelterManager.model.NoEmptySpacesException;
import ski.serwon.AnimalShelterManager.model.Species;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * AnimalDatabase represents part of database which is
 * responsible for keeping list of animals being in shelter.
 *
 *
 * @version 1.0
 * @since 1.0
 */
public class AnimalDatabase {
    public static final String SELECT_ALL_ANIMALS_FROM_DATABASE = "SELECT * FROM " + Database.TABLE_ANIMALS;

    public static final String SELECT_ANIMALS_OF_SPECIFIED_SPECIES = "SELECT * FROM " + Database.TABLE_ANIMALS
            + " JOIN " + Database.TABLE_BREEDS + " ON " + Database.TABLE_ANIMALS + "." + Database.ANIMALS_COLUMN_BREED
            + " = " + Database.TABLE_BREEDS + "." + Database.BREEDS_COLUMN_ID + " JOIN " + Database.TABLE_SPECIES
            + " ON " + Database.TABLE_BREEDS + "." + Database.BREEDS_COLUMN_SPECIES + " = " + Database.TABLE_SPECIES
            + "." + Database.SPECIES_COLUMN_ID + " WHERE " + Database.TABLE_SPECIES + "."
            + Database.SPECIES_COLUMN_ID + " = ?";

    public static final String SELECT_ANIMALS_OF_SPECIFIED_BREED = "SELECT * FROM " + Database.TABLE_ANIMALS + " JOIN "
            + Database.TABLE_BREEDS + " ON " + Database.TABLE_ANIMALS + "." + Database.ANIMALS_COLUMN_BREED + " = "
            + Database.TABLE_BREEDS + "." + Database.BREEDS_COLUMN_ID + " WHERE " + Database.TABLE_BREEDS + "."
            + Database.BREEDS_COLUMN_ID + " = ?";

    public static final String INSERT_NEW_ANIMAL = "INSERT INTO " + Database.TABLE_ANIMALS
            + "(" + Database.ANIMALS_COLUMN_BREED + ", " + Database.ANIMALS_COLUMN_NAME + ", "
            + Database.ANIMALS_COLUMN_SEX + ", " + Database.ANIMALS_COLUMN_BIRTHDATE + ", "
            + Database.ANIMALS_COLUMN_IN_SHELTER_SINCE + ", "
            + Database.ANIMALS_COLUMN_LAST_WALK + ") VALUES(?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_EXISTING_ANIMAL = "UPDATE " + Database.TABLE_ANIMALS + " SET "
            + Database.ANIMALS_COLUMN_BREED + " = ?, " + Database.ANIMALS_COLUMN_NAME + " = ?, "
            + Database.ANIMALS_COLUMN_SEX + " = ?, " + Database.ANIMALS_COLUMN_BIRTHDATE + " = ? "
            + "WHERE " + Database.ANIMALS_COLUMN_ID + " = ?";

    public static final String REMOVE_ANIMAL = "DELETE FROM " + Database.TABLE_ANIMALS
            + " WHERE " + Database.ANIMALS_COLUMN_ID + " = ?";

    public static final String UPDATE_LAST_WALK = "UPDATE " + Database.TABLE_ANIMALS
            + " SET " + Database.ANIMALS_COLUMN_LAST_WALK + " = ? "
            + "WHERE " + Database.ANIMALS_COLUMN_ID + " = ?";

    public static final String COUNT_ALL_ANIMALS = "SELECT COUNT(*) FROM " + Database.TABLE_ANIMALS;

    public static final String SELECT_MAX_USED_ID = "SELECT MAX(" + Database.ANIMALS_COLUMN_ID + ") "
            + "FROM " + Database.TABLE_ANIMALS;

    public static final String COUNT_ANIMALS_OF_SPECIFIED_SPECIES = "SELECT COUNT("
            + Database.TABLE_ANIMALS + "." + Database.ANIMALS_COLUMN_ID + ") FROM "
            + Database.TABLE_ANIMALS + " JOIN " + Database.TABLE_BREEDS + " ON "
            + Database.TABLE_ANIMALS + "." + Database.ANIMALS_COLUMN_BREED + " = "
            + Database.TABLE_BREEDS + "." + Database.BREEDS_COLUMN_ID
            + " WHERE " + Database.TABLE_BREEDS + "." + Database.BREEDS_COLUMN_SPECIES + " = ?";

    /**
     * Singleton instance of class.
     */
    private static AnimalDatabase instance = new AnimalDatabase();

    /**
     * Last (highest number) used ID in database.
     */
    private int lastUsedId;

    /**
     * List of all animals in shelter.
     */
    private ObservableList<Animal> animals;

    /**
     * Class constructor.
     */
    private AnimalDatabase() {
        animals = FXCollections.observableList(getAllAnimalsFromDatabase());
        lastUsedId = getLastAnimalId();
    }

    /**
     * Method connects to database and loads every record from ANIMALS table.
     * Each record from database is then used to create one {@link Animal} object.
     *
     * @return List of {@link Animal} objects loaded from database.
     */
    public static List<Animal> getAllAnimalsFromDatabase() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_ANIMALS_FROM_DATABASE)) {

            List<Animal> toReturn = new LinkedList<>();

            if (resultSet.next()) {
                int idColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_ID);
                int breedColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BREED);
                int nameColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_NAME);
                int sexColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_SEX);
                int birthDateColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BIRTHDATE);
                int inShelterSinceColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_IN_SHELTER_SINCE);
                int lastWalkColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_LAST_WALK);

                do {
                    int id = resultSet.getInt(idColumn);
                    int breedId = resultSet.getInt(breedColumn);
                    Breed breed = BreedDatabase.getInstance().getBreedViaId(breedId);
                    String name = resultSet.getString(nameColumn);
                    Animal.Sex sex = Animal.Sex.getSexFromString(resultSet.getString(sexColumn));
                    LocalDate birthDate = LocalDate.parse(resultSet.getString(birthDateColumn));
                    LocalDate inShelterSince = LocalDate.parse(resultSet.getString(inShelterSinceColumn));
                    LocalDate lastWalk;
                    try {
                        lastWalk = LocalDate.parse(resultSet.getString(lastWalkColumn));
                    } catch (NullPointerException e) {
                        lastWalk = null;
                    }
                    toReturn.add(new Animal(sex, name, birthDate, inShelterSince, breed, lastWalk, id));
                } while (resultSet.next());
            }

            return toReturn;
        } catch (SQLException e) {
            e.printStackTrace();
            return new LinkedList<>();
        }
    }

    /**
     * Method connects to database and count how many
     * animals of specified species are currently in database.
     * @param speciesId ID of species in database
     * @return Number of animals of specified species.
     */
    public static int countAnimalOfSpecifiedSpecies(int speciesId) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(COUNT_ANIMALS_OF_SPECIFIED_SPECIES)) {
            preparedStatement.setInt(1, speciesId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return -2;
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    /**
     * Method connects to database and loads record(s) representing animal(s) of selected species
     * from ANIMALS table.
     * Each record from database is then used to create one {@link Animal} object.
     *
     * @param species Selected species
     * @return List of {@link Animal} objects loaded from database.
     */
    public static List<Animal> getAnimalsOfSpecifiedSpecies(Species species) {
        ResultSet resultSet = null;
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ANIMALS_OF_SPECIFIED_SPECIES)) {

            preparedStatement.setInt(1, species.getId());
            resultSet = preparedStatement.executeQuery();
            List<Animal> toReturn = new LinkedList<>();

            if (resultSet.next()) {
                int idColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_ID);
                int breedColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BREED);
                int nameColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_NAME);
                int sexColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_SEX);
                int birthDateColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BIRTHDATE);
                int inShelterSinceColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_IN_SHELTER_SINCE);
                int lastWalkColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_LAST_WALK);

                do {
                    int id = resultSet.getInt(idColumn);
                    int breedId = resultSet.getInt(breedColumn);
                    Breed breed = BreedDatabase.getInstance().getBreedViaId(breedId);
                    String name = resultSet.getString(nameColumn);
                    Animal.Sex sex = Animal.Sex.getSexFromString(resultSet.getString(sexColumn));
                    LocalDate birthDate = LocalDate.parse(resultSet.getString(birthDateColumn));
                    LocalDate inShelterSince = LocalDate.parse(resultSet.getString(inShelterSinceColumn));
                    LocalDate lastWalk;
                    try {
                        lastWalk = LocalDate.parse(resultSet.getString(lastWalkColumn));
                    } catch (NullPointerException e) {
                        lastWalk = null;
                    }
                    toReturn.add(new Animal(sex, name, birthDate, inShelterSince, breed, lastWalk, id));
                } while (resultSet.next());
            }

            return toReturn;

        } catch (SQLException e) {
            //todo
            return new LinkedList<>();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Method connects to database and loads record(s) representing animals of selected breed
     * from ANIMALS table.
     * Every record from database is then used to create one {@link Animal} object.
     *
     * @param breed Selected breed
     * @return List of {@link Animal} objects loaded from database.
     */
    public static List<Animal> getAnimalsOfSpecifiedBreed(Breed breed) {
        ResultSet resultSet = null;
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ANIMALS_OF_SPECIFIED_BREED)) {

            preparedStatement.setInt(1, breed.getId());
            resultSet = preparedStatement.executeQuery();
            List<Animal> toReturn = new LinkedList<>();

            if (resultSet.next()) {

                int idColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_ID);
                int nameColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_NAME);
                int sexColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_SEX);
                int birthDateColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BIRTHDATE);
                int inShelterSinceColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_IN_SHELTER_SINCE);
                int lastWalkColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_LAST_WALK);

                do {
                    int id = resultSet.getInt(idColumn);
                    String name = resultSet.getString(nameColumn);
                    Animal.Sex sex = Animal.Sex.getSexFromString(resultSet.getString(sexColumn));
                    LocalDate birthDate = LocalDate.parse(resultSet.getString(birthDateColumn));
                    LocalDate inShelterSince = LocalDate.parse(resultSet.getString(inShelterSinceColumn));
                    LocalDate lastWalk;
                    try {
                        lastWalk = LocalDate.parse(resultSet.getString(lastWalkColumn));
                    } catch (NullPointerException e) {
                        lastWalk = null;
                    }
                    toReturn.add(new Animal(sex, name, birthDate, inShelterSince, breed, lastWalk, id));
                } while (resultSet.next());
            }

            return toReturn;

        } catch (SQLException e) {
            return new LinkedList<>();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    public static AnimalDatabase getInstance() {
        return instance;
    }

    /**
     * Method connects to database and
     * inserts passed {@link Animal} object into is.
     * @param animal {@link Animal} object to insert into database.
     * @return true if succeeded; false otherwise
     */
    private boolean insertNewAnimal(Animal animal) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement =
                     connection.prepareStatement(INSERT_NEW_ANIMAL)) {

            preparedStatement.setInt(1, animal.getBreed().getId());
            preparedStatement.setString(2, animal.getName());
            preparedStatement.setString(3, animal.getSex().toString());
            preparedStatement.setString(4, animal.getBirthDate().toString());
            preparedStatement.setString(5, animal.getInShelterSince().toString());
            try {
                preparedStatement.setString(6, animal.getLastWalk().toString());
            } catch (NullPointerException e) {
                preparedStatement.setDate(6, null);
            }

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Sets passed {@link Animal} object's fields to new values.
     * Passed values can be the same as they were before.
     *
     * @param animal {@link Animal} object which fields are to be changed
     * @param updatedBreed New value of {@link Animal#breed} field
     * @param updatedName New value of {@link Animal#name} field
     * @param updatedSex New value of {@link Animal#sex} field
     * @param updatedBirthDate New value of {@link Animal#birthDate} field
     * @return true if succeeded; false otherwise
     */
    public boolean editAnimal(Animal animal, Breed updatedBreed, String updatedName,
                              Animal.Sex updatedSex, LocalDate updatedBirthDate) {
        if (animal != null && updateExistingAnimalInDatabase(animal.getId(),
                updatedBreed, updatedName, updatedSex, updatedBirthDate)) {
            animal.setBreed(updatedBreed);
            animal.setName(updatedName);
            animal.setSex(updatedSex);
            animal.setBirthDate(updatedBirthDate);

            return true;
        }
        return false;
    }

    /**
     * Connects to database and sets record with passed ID to new values.
     * Values can be the same as they were before
     *
     * @param updatedAnimalsId Selected record ID.
     * @param updatedBreed New value of Breed's ID
     * @param updatedName New value of name.
     * @param updatedSex New value of {@link Animal#sex} enum (toString)
     * @param updatedDate New value of {@link Animal#birthDate}
     * @return true if succeeded; false otherwise
     */
    private boolean updateExistingAnimalInDatabase(int updatedAnimalsId, Breed updatedBreed,
                                                   String updatedName, Animal.Sex updatedSex, LocalDate updatedDate) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_EXISTING_ANIMAL)) {

            preparedStatement.setInt(1, updatedBreed.getId());
            preparedStatement.setString(2, updatedName);
            preparedStatement.setString(3, updatedSex.toString());
            preparedStatement.setString(4, updatedDate.toString());
            preparedStatement.setInt(5, updatedAnimalsId);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Deletes animal from database.
     * @param animalToDelete {@link Animal} object which is to be deleted.
     * @return true if succeeded; false otherwise
     */
    public boolean deleteAnimal(Animal animalToDelete) {
        if (animalToDelete != null && removeAnimalFromDatabase(animalToDelete)) {
            animals.remove(animalToDelete);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Connects to database and removes record containing information about passed
     * {@link Animal} object.
     *
     * @param animalToRemove {@link Animal} object which is to be deleted.
     * @return true if succeeded; false otherwise
     */
    private boolean removeAnimalFromDatabase(Animal animalToRemove) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(REMOVE_ANIMAL)) {

            preparedStatement.setInt(1, animalToRemove.getId());

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            //todo
            return false;
        }
    }

    /**
     * Sets {@link Animal#lastWalk} to current date.
     * @param animal Selected {@link Animal} object
     * @return true if animal requires to be walked out and operation succeeded; false otherwise
     */
    public boolean walkOutAnimal(Animal animal) {
        LocalDate newDate = LocalDate.now();
        if (animal.getBreed().doesRequireWalk() && updateLaskWalkDate(newDate, animal)) {
            animal.setLastWalk(newDate);

            return true;
        }
        return false;
    }

    /**
     * Connects to database and updates date of last walk in record containing info about
     * passed {@link Animal} object.
     *
     * @param updatedDate New value of date
     * @param walkedAnimal Selected {@link Animal} object
     * @return true if succeeded; false otherwise
     */
    private boolean updateLaskWalkDate(LocalDate updatedDate, Animal walkedAnimal) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_LAST_WALK)) {

            preparedStatement.setString(1, updatedDate.toString());
            preparedStatement.setInt(2, walkedAnimal.getId());

            return preparedStatement.executeUpdate() == 1;

        } catch (SQLException e) {
            //todo
            return false;
        }
    }

    /**
     * Connects to database and counts number of records in ANIMALS table.
     *
     * @return Number of animals in database. -1 in case of SQLException;
     * -2 if query result failed
     */
    public int countAnimals() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(COUNT_ALL_ANIMALS)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return -2;
            }

        } catch (SQLException e) {
            return -1;
        }
    }

    /**
     * Connects to database and loads ID of last record in ANIMALS table.
     *
     * @return ID of last animal; -1 in case of SQLException
     * or -2 if query result failed
     */
    private int getLastAnimalId() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_MAX_USED_ID)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return -2;
            }
        } catch (SQLException e) {
            return -1;
        }
    }


    public ObservableList<Animal> getAnimals() {
        return animals;
    }


    /**
     * Creates new {@link Animal} object and adds it to database.
     *
     * @param sex New animal's {@link ski.serwon.AnimalShelterManager.model.Animal.Sex}
     * @param name New animal's {@link Animal#name}
     * @param birthDate New animal's {@link Animal#birthDate}
     * @param breed New animal's {@link Breed}
     * @return true if adding to database succeeded; false otherwise
     * @throws NoEmptySpacesException if all places for specified species are occupied
     */
    public boolean addAnimal(Animal.Sex sex, String name, LocalDate birthDate, Breed breed)
    throws NoEmptySpacesException{
        Species species = SpeciesDatabase.getInstance().getSpeciesById
                ((BreedDatabase.getInstance().getSpeciesIdForBreed(breed)));
        if (species.getPlacesLimit() < 1) {
            throw new NoEmptySpacesException("There is no empty space for selected species.");
        }

        LocalDate inShelterSince = LocalDate.now();
        LocalDate lastWalk = breed.doesRequireWalk() ? LocalDate.now() : null;
        int animalID = ++lastUsedId;

        Animal newAnimal = new Animal(sex, name, birthDate, inShelterSince, breed, lastWalk, animalID);
        return insertNewAnimal(newAnimal) && animals.add(newAnimal);
    }
}
