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

    private static AnimalDatabase instance = new AnimalDatabase();
    private int lastUsedId;
    private ObservableList<Animal> animals;


    private AnimalDatabase() {
        animals = FXCollections.observableList(getAllAnimalsFromDatabase());
        lastUsedId = getLastAnimalId();
    }


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
//                int breedColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BREED);
                int nameColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_NAME);
                int sexColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_SEX);
                int birthDateColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BIRTHDATE);
                int inShelterSinceColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_IN_SHELTER_SINCE);
                int lastWalkColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_LAST_WALK);

                do {
                    int id = resultSet.getInt(idColumn);
//                    int breedId = resultSet.getInt(breedColumn);
//                    Breed breed = BreedDatabase.getInstance().getBreedViaId(breedId);
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

    public static AnimalDatabase getInstance() {
        return instance;
    }

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
            //todo
            return false;
        }
    }

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

    private boolean updateExistingAnimalInDatabase(int updatesAnimalsId, Breed updatedBreed,
                                                   String updatedName, Animal.Sex updatedSex, LocalDate updatedDate) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_EXISTING_ANIMAL)) {

            preparedStatement.setInt(1, updatedBreed.getId());
            preparedStatement.setString(2, updatedName);
            preparedStatement.setString(3, updatedSex.toString());
            preparedStatement.setString(4, updatedDate.toString());
            preparedStatement.setInt(5, updatesAnimalsId);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            //todo
            return false;
        }
    }

    public boolean deleteAnimal(Animal animalToDelete) {
        if (animalToDelete != null && removeAnimalFromDatabase(animalToDelete)) {
            animals.remove(animalToDelete);
            return true;
        } else {
            return false;
        }
    }

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

    public boolean walkOutAnimal(Animal animal) {
        LocalDate newDate = LocalDate.now();
        if (animal.getBreed().doesRequireWalk() && updateLaskWalkDate(newDate, animal)) {
            animal.setLastWalk(newDate);

            return true;
        }
        return false;
    }

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

    public int countAnimals() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(COUNT_ALL_ANIMALS)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                throw new SQLException("Query execution failed.");
            }

        } catch (SQLException e) {
            //todo -- handle SQLException
            return -1;
        }
    }

    public int getLastAnimalId() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_MAX_USED_ID)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return -2;
            }
        } catch (SQLException e) {
            //TODO
            return -1;
        }
    }


    public ObservableList<Animal> getAnimals() {
        return animals;
    }

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
