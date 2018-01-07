package ski.serwon.AnimalShelterManager.model.datamodel;

import ski.serwon.AnimalShelterManager.model.Breed;
import ski.serwon.AnimalShelterManager.model.Species;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class BreedDatabase {
    private static BreedDatabase instance = new BreedDatabase();
    private int lastUsedId;


    private BreedDatabase() {
        lastUsedId = getLastUsedId();
    }

    public static BreedDatabase getInstance() {
        return instance;
    }

    public static final String SELECT_ALL_BREEDS = "SELECT * FROM " + Database.TABLE_BREEDS;

    public static final String SELECT_BREEDS_OF_SPECIFIED_SPECIES = "SELECT * FROM "
            + Database.TABLE_BREEDS + " WHERE "
            + Database.BREEDS_COLUMN_SPECIES + " = ?";

    public static final String INSERT_NEW_BREED = "INSERT INTO " + Database.TABLE_BREEDS + "("
            + Database.BREEDS_COLUMN_NAME + ", "
            + Database.BREEDS_COLUMN_SPECIES + ", "
            + Database.BREEDS_COLUMN_REQUIRE_WALKS
            + ") VALUES (?, ?, ?)";

    public static final String COUNT_ALL_BREEDS = "SELECT COUNT(*) FROM " + Database.TABLE_BREEDS;

    public static final String SELECT_SPECIES_ID_FOR_SPECIFIED_BREED = "SELECT " + Database.BREEDS_COLUMN_SPECIES
            + " FROM " + Database.TABLE_BREEDS + " WHERE " + Database.BREEDS_COLUMN_ID + " = ?";

    public static final String SELECT_MAX_USED_ID = "SELECT MAX(" + Database.BREEDS_COLUMN_ID + ") "
            + "FROM " + Database.TABLE_BREEDS;

    public static final String UPDATE_EXISTING_BREED = "UPDATE " + Database.TABLE_BREEDS
            + " SET " + Database.BREEDS_COLUMN_REQUIRE_WALKS + " = ? WHERE "
            + Database.BREEDS_COLUMN_ID + " = ?";

    public List<Breed> getBreedsOfSpecifiedSpecies(Species species) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BREEDS_OF_SPECIFIED_SPECIES)) {

            preparedStatement.setInt(1, species.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Breed> toReturn = new LinkedList<>();

            if (resultSet.next()) {
                int idColumn = resultSet.findColumn(Database.BREEDS_COLUMN_ID);
                int nameColumn = resultSet.findColumn(Database.BREEDS_COLUMN_NAME);
                int requireWalksColumn = resultSet.findColumn(Database.BREEDS_COLUMN_REQUIRE_WALKS);


                do {
                    String name = resultSet.getString(nameColumn);
                    int id = resultSet.getInt(idColumn);
                    boolean requireWalks = resultSet.getBoolean(requireWalksColumn);

                    toReturn.add(new Breed(name, requireWalks, id));
                } while (resultSet.next());
            }

            return toReturn;

        } catch (SQLException e) {
            //todo
            return new LinkedList<>();
        }
    }

    public List<Breed> getAllBreeds() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_BREEDS)) {

            List<Breed> toReturn = new LinkedList<>();

            if (resultSet.next()) {
                int idColumn = resultSet.findColumn(Database.BREEDS_COLUMN_ID);
                int nameColumn = resultSet.findColumn(Database.BREEDS_COLUMN_NAME);
                int requireWalksColumn = resultSet.findColumn(Database.BREEDS_COLUMN_REQUIRE_WALKS);


                do {
                    String name = resultSet.getString(nameColumn);
                    int id = resultSet.getInt(idColumn);
                    boolean requireWalks = resultSet.getBoolean(requireWalksColumn);

                    toReturn.add(new Breed(name, requireWalks, id));
                } while (resultSet.next());
            }

            return toReturn;

        } catch (SQLException e) {
            //todo
            return null;
        }
    }

    public int countAllBreeds() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(COUNT_ALL_BREEDS)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return -2;
            }

        } catch (SQLException e) {
            //todo
            return -1;
        }
    }

    private int getLastUsedId() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_MAX_USED_ID)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return -2;
            }

        } catch (SQLException e) {
            //todo
            return -1;
        }
    }

    private boolean addBreedToDatabase(Breed breed, int speciesId) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_BREED)) {

            preparedStatement.setString(1, breed.getName());
            preparedStatement.setInt(2, speciesId);
            preparedStatement.setBoolean(3, breed.doesRequireWalk());

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            //todo
            return false;
        }
    }


    public boolean addBreedToSpecies(Species species, String name, boolean requiresWalk) {
        if (species.getBreeds()
                .stream()
                .map(Breed::getName)
                .anyMatch(c -> c.equals(name))) {
            return false;
        }

        Breed toAdd = new Breed(name, requiresWalk, ++lastUsedId);

        return species.getBreeds().add(toAdd)
                && addBreedToDatabase(toAdd, species.getId());
    }

    public Breed getBreedViaId(int id) {
        int speciesId;

        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SELECT_SPECIES_ID_FOR_SPECIFIED_BREED)) {

            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                speciesId = resultSet.getInt(1);
            } else {
                return null;
            }

        } catch (SQLException e) {
            //todo
            return null;
        }

        Optional<Breed> toReturn = SpeciesDatabase.getInstance().getSpeciesById(speciesId)
                .getBreeds()
                .stream()
                .filter(c -> c.getId() == id)
                .findAny();

        if (toReturn.isPresent()) {
            return toReturn.get();
        } else {
            return null;
        }
    }

    public boolean editBreed(Breed breed, boolean doesRequireWalk) {
        if (updateExistingBreed(doesRequireWalk, breed.getId())) {
            breed.setRequireWalk(doesRequireWalk);
            return true;
        }
        return false;
    }

    private boolean updateExistingBreed(boolean requiresWalk, int id) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
        PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_EXISTING_BREED)) {
            preparedStatement.setBoolean(1, requiresWalk);
            preparedStatement.setInt(2, id);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
           return false;
        }
    }
}
