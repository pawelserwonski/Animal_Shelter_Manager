package ski.serwon.AnimalShelterManager.model.datamodel;

import ski.serwon.AnimalShelterManager.model.Breed;
import ski.serwon.AnimalShelterManager.model.Species;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * BreedDatabase represents part of database which is
 * responsible for keeping list of Breeds.
 *
 * @version 1.0
 * @since 1.0
 */
public class BreedDatabase {
    /**
     * Singleton instance of class.
     */
    private static BreedDatabase instance = new BreedDatabase();

    /**
     * Last (highest number) used ID in database.
     */
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

    public static final String SELECT_SPECIES_ID_FOR_BREED = "SELECT " + Database.BREEDS_COLUMN_SPECIES
            + " FROM " + Database.TABLE_BREEDS + " WHERE " + Database.BREEDS_COLUMN_ID + " = ?";


    /**
     * Method connects to database and loads record(s) representing breed(s) of selected species
     * from BREEDS table.
     * Each record from database is then used to create one {@link Breed} object.
     *
     * @param species Selected species
     * @return List of {@link Breed} objects loaded from database.
     */
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
            return new LinkedList<>();
        }
    }

    /**
     * Method connects to database and loads every record from BREEDS table.
     * Each record from database is then used to create one {@link Breed} object.
     *
     * @return List of {@link Breed} objects loaded from database.
     */
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
            return null;
        }
    }

    /**
     * Connects to database and loads ID of species
     * which has passed {@link Breed} on its list.
     *
     * @param breed Selected {@link Breed}
     * @return species id; -1 if breed is null; -2 in case of SQLException
     * -3 if query didn't return any value
     */
    public int getSpeciesIdForBreed(Breed breed) {
        if (breed == null) {
            return -1;
        }

        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement statement = connection.prepareStatement
                     (SELECT_SPECIES_ID_FOR_SPECIFIED_BREED)) {
            statement.setInt(1, breed.getId());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return -3;
        } catch (SQLException e) {
            return -2;
        }
    }

    /**
     * Connects to database and counts number of records in BREEDS table.
     *
     * @return Number of breeds in database. -1 in case of SQLException;
     *  -2 if query didn't return any value
     */
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
            return -1;
        }
    }

    /**
     * Connects to database and loads ID of last record in BREEDS table.
     *
     * @return ID of last breed; -1 in case of SQLException
     *  -2 if query didn't return any value
     */
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
            return -1;
        }
    }

    /**
     * Method connects to database and
     * inserts passed {@link Breed} object into is.
     * @param breed object to insert into database
     * @param speciesId id of species to whom new breed belongs
     * @return true if succeeded; false otherwise
     */
    private boolean insertNewBreed(Breed breed, int speciesId) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_BREED)) {

            preparedStatement.setString(1, breed.getName());
            preparedStatement.setInt(2, speciesId);
            preparedStatement.setBoolean(3, breed.doesRequireWalk());

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Checks if breed with passed name exists. If not adds new breed
     * to database and connects it with passed species.
     * @param species {@link Species} to whom new breed belongs
     * @param name New breed's name
     * @param requiresWalk Does animals of new breed require to be walked out.
     * @return true if there was not such breed and new object was added successful;
     *  false otherwise
     */
    public boolean addBreedToSpecies(Species species, String name, boolean requiresWalk) {
        if (species.getBreeds()
                .stream()
                .map(Breed::getName)
                .anyMatch(c -> c.equals(name))) {
            return false;
        }

        Breed toAdd = new Breed(name, requiresWalk, ++lastUsedId);

        return species.getBreeds().add(toAdd)
                && insertNewBreed(toAdd, species.getId());
    }

    /**
     * Method search for {@link Breed} object with ID as passed.
     *
     * @param id ID to search for
     * @return Object with passed id; null if query didn't return any result, SQLException
     * or problem with Stream
     */
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

    /**
     * Sets selected {@link Breed} field to value passed as parameter.
     * Value can be the same as before
     *
     * @param breed Selected {@link Breed}
     * @param doesRequireWalk New value of {@link Breed#requireWalk}
     * @return true if succeeded; false otherwise
     *
     * @see #updateExistingBreed(boolean, int)
     */
    public boolean editBreed(Breed breed, boolean doesRequireWalk) {
        if (updateExistingBreed(doesRequireWalk, breed.getId())) {
            breed.setRequireWalk(doesRequireWalk);
            return true;
        }
        return false;
    }

    /**
     * Connects to database and edits record from table BREEDS
     * with ID as passed
     * @param requiresWalk new value of {@link Breed#requireWalk}
     * @param id ID of edited breed
     * @return true if succeeded; false otherwise
     * @see #editBreed(Breed, boolean)
     */
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
