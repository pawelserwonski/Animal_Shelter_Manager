package ski.serwon.AnimalShelterManager.model.datamodel;

import ski.serwon.AnimalShelterManager.model.Animal;
import ski.serwon.AnimalShelterManager.model.Breed;
import ski.serwon.AnimalShelterManager.model.Species;

import javax.xml.crypto.Data;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;


public class AnimalDatabase {
    private static int lastUsedId;
    private static AnimalDatabase instance = new AnimalDatabase();

    private List<Animal> animals;

    private AnimalDatabase() {
        animals = new LinkedList<>();
        lastUsedId = getLastAnimalId();
    }

    public static final String SELECT_ALL_ANIMALS_FROM_DATABASE = "SELECT * FROM " + Database.TABLE_ANIMALS;

    public static final String SELECT_ANIMALS_OF_SPECIFIED_SPECIES = "SELECT * FROM " + Database.TABLE_ANIMALS
            + "JOIN " + Database.TABLE_BREEDS + " ON " + Database.TABLE_ANIMALS + "."
            + Database.ANIMALS_COLUMN_BREED + " = " + Database.TABLE_BREEDS + "."
            + Database.BREEDS_COLUMN_ID + " JOIN " + Database.TABLE_SPECIES + " ON "
            + Database.TABLE_BREEDS + "." + Database.BREEDS_COLUMN_SPECIES + " = "
            + Database.TABLE_SPECIES + "." + Database.SPECIES_COLUMN_ID + " WHERE "
            + Database.TABLE_SPECIES + "." + Database.SPECIES_COLUMN_ID + " = ?";

    public static final String SELECT_ANIMALS_OF_SPECIFIED_BREED = "SELECT * FROM "
            + Database.TABLE_ANIMALS + "JOIN " + Database.TABLE_BREEDS
            + " ON " + Database.TABLE_ANIMALS + "." + Database.ANIMALS_COLUMN_BREED
            + " = " + Database.TABLE_BREEDS + "." + Database.BREEDS_COLUMN_ID
            + " WHERE " + Database.TABLE_BREEDS + "." + Database.BREEDS_COLUMN_ID + " = ?";

    public static final String INSERT_NEW_ANIMAL = "INSERT INTO " + Database.TABLE_ANIMALS + "(" +
            Database.ANIMALS_COLUMN_BREED + ", "
            + Database.ANIMALS_COLUMN_NAME + ", "
            + Database.ANIMALS_COLUMN_SEX + ", "
            + Database.ANIMALS_COLUMN_BIRTHDATE + ", "
            + Database.ANIMALS_COLUMN_IN_SHELTER_SINCE + ", "
            + Database.ANIMALS_COLUMN_LAST_WALK
            + ") VALUES(?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_EXISTING_ANIMAL = "UPDATE " + Database.TABLE_ANIMALS + " SET " +
            Database.ANIMALS_COLUMN_BREED + " = ?, "
            + Database.ANIMALS_COLUMN_NAME + " = ?, "
            + Database.ANIMALS_COLUMN_SEX + " = ?, "
            + Database.ANIMALS_COLUMN_BIRTHDATE + " = ?, "
            + Database.ANIMALS_COLUMN_IN_SHELTER_SINCE + " = ?"
            + " WHERE " + Database.ANIMALS_COLUMN_ID + " = ?";

    public static final String REMOVE_ANIMAL = "DELETE FROM " + Database.TABLE_ANIMALS
            + " WHERE " + Database.ANIMALS_COLUMN_ID + " = ?";

    public static final String UPDATE_LAST_WALK = "UPDATE " + Database.TABLE_ANIMALS
            + " SET " + Database.ANIMALS_COLUMN_LAST_WALK + " = ? "
            + "WHERE " + Database.ANIMALS_COLUMN_ID + " = ?";

    public static final String COUNT_ALL_ANIMALS = "SELECT COUNT(*) FROM " + Database.TABLE_ANIMALS;

    public static List<Animal> getAllAnimalsFromDatabase() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_ANIMALS_FROM_DATABASE)) {

            List<Animal> toReturn = new LinkedList<>();

            int idColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_ID);
            int breedColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BREED);
            int nameColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_NAME);
            int sexColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_SEX);
            int birthDateColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BIRTHDATE);
            int inShelterSinceColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_IN_SHELTER_SINCE);
            int lastWalkColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_LAST_WALK);

            while (resultSet.next()) {
                int id = resultSet.getInt(idColumn);
                int breedId = resultSet.getInt(breedColumn);
                Breed breed = BreedDatabase.getInstance().getBreedViaId(breedId);
                String name = resultSet.getString(nameColumn);
                Animal.Sex sex = resultSet.getString(sexColumn).toLowerCase().equals("m") ? Animal.Sex.male : Animal.Sex.female;
                LocalDate birthDate = resultSet.getDate(birthDateColumn).toLocalDate();
                LocalDate inShelterSince = resultSet.getDate(inShelterSinceColumn).toLocalDate();
                LocalDate lastWalk = resultSet.getDate(lastWalkColumn).toLocalDate();
                toReturn.add(new Animal(sex, name, birthDate, inShelterSince, breed, lastWalk, id));
            }

            return toReturn;
        } catch (SQLException e) {
            //TODO
            return null;
        }
    }

    public static List<Animal> getAnimalsOfSpecifiedSpecies(Species species) {
        ResultSet resultSet = null;
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ANIMALS_OF_SPECIFIED_SPECIES)) {

            preparedStatement.setInt(1, species.getId());
            resultSet = preparedStatement.executeQuery();
            List<Animal> toReturn = new LinkedList<>();

            int idColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_ID);
            int breedColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BREED);
            int nameColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_NAME);
            int sexColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_SEX);
            int birthDateColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BIRTHDATE);
            int inShelterSinceColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_IN_SHELTER_SINCE);
            int lastWalkColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_LAST_WALK);

            while (resultSet.next()) {
                int id = resultSet.getInt(idColumn);
                int breedId = resultSet.getInt(breedColumn);
                Breed breed = BreedDatabase.getInstance().getBreedViaId(breedId);
                String name = resultSet.getString(nameColumn);
                Animal.Sex sex = resultSet.getString(sexColumn).toLowerCase().equals("m") ? Animal.Sex.male : Animal.Sex.female;
                LocalDate birthDate = resultSet.getDate(birthDateColumn).toLocalDate();
                LocalDate inShelterSince = resultSet.getDate(inShelterSinceColumn).toLocalDate();
                LocalDate lastWalk = resultSet.getDate(lastWalkColumn).toLocalDate();
                toReturn.add(new Animal(sex, name, birthDate, inShelterSince, breed, lastWalk, id));
            }

            return toReturn;

        } catch (SQLException e) {
            //todo
            return null;
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
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ANIMALS_OF_SPECIFIED_BREED)) {

            preparedStatement.setInt(1, breed.getId());
            resultSet = preparedStatement.executeQuery();
            List<Animal> toReturn = new LinkedList<>();

            int idColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_ID);
            int breedColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BREED);
            int nameColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_NAME);
            int sexColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_SEX);
            int birthDateColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_BIRTHDATE);
            int inShelterSinceColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_IN_SHELTER_SINCE);
            int lastWalkColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_LAST_WALK);

            while (resultSet.next()) {
                int id = resultSet.getInt(idColumn);
                int breedId = resultSet.getInt(breedColumn);
                Breed breed = BreedDatabase.getInstance().getBreedViaId(breedId);
                String name = resultSet.getString(nameColumn);
                Animal.Sex sex = resultSet.getString(sexColumn).toLowerCase().equals("m") ? Animal.Sex.male : Animal.Sex.female;
                LocalDate birthDate = resultSet.getDate(birthDateColumn).toLocalDate();
                LocalDate inShelterSince = resultSet.getDate(inShelterSinceColumn).toLocalDate();
                LocalDate lastWalk = resultSet.getDate(lastWalkColumn).toLocalDate();
                toReturn.add(new Animal(sex, name, birthDate, inShelterSince, breed, lastWalk, id));
            }

            return toReturn;

        } catch (SQLException e) {
            //todo
            return null;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    public boolean insertNewAnimal(Animal animal) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_ANIMAL)) {

            preparedStatement.setInt(1, animal.getBreed().getId());
            preparedStatement.setString(2, animal.getName());
            preparedStatement.setString(3, animal.getSex() == Animal.Sex.male ? "m" : "f");
            preparedStatement.setDate(4, Date.valueOf(animal.getBirthDate()));
            preparedStatement.setDate(5, Date.valueOf(animal.getInShelterSince()));
            preparedStatement.setDate(6, Date.valueOf(animal.getLastWalk()));

            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            //todo
            return false;
        }
    }
//
//    public static final String INSERT_NEW_ANIMAL = "INSERT INTO " + Database.TABLE_ANIMALS + "(" +
//            Database.ANIMALS_COLUMN_BREED + ", "
//            + Database.ANIMALS_COLUMN_NAME + ", "
//            + Database.ANIMALS_COLUMN_SEX + ", "
//            + Database.ANIMALS_COLUMN_BIRTHDATE + ", "
//            + Database.ANIMALS_COLUMN_IN_SHELTER_SINCE + ", "
//            + Database.ANIMALS_COLUMN_LAST_WALK
//            + ") VALUES(?, ?, ?, ?, ?, ?)";

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
        }

    }

    public int getLastAnimalId() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_ANIMALS_FROM_DATABASE)) {

            int idColumn = resultSet.findColumn(Database.ANIMALS_COLUMN_ID);
            if (resultSet.last()) {
                return resultSet.getInt(idColumn);
            } else {
                return -2;
            }
        } catch (SQLException e) {
            //TODO
            return -1;
        }
    }

    public static AnimalDatabase getInstance() {
        return instance;
    }

    public List<Animal> getAnimals() {
        return animals;
    }
}
