package ski.serwon.AnimalShelterManager.model.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ski.serwon.AnimalShelterManager.model.Species;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * SpeciesDatabase represents part of database which is
 * responsible for keeping list of species.
 *
 *
 * @version 1.0
 * @since 1.0
 */
public class SpeciesDatabase {

    public static final String SELECT_ALL_SPECIES = "SELECT * FROM " + Database.TABLE_SPECIES;

    public static final String INSERT_NEW_SPECIES = "INSERT INTO " + Database.TABLE_SPECIES
            + "(" + Database.SPECIES_COLUMN_NAME + ", "
            + Database.SPECIES_COLUMN_LIMIT
            + ") VALUES (?, ?)";

    public static final String UPDATE_LIMITS = "UPDATE " + Database.TABLE_SPECIES
            + " SET " + Database.SPECIES_COLUMN_LIMIT + " = ? "
            + "WHERE " + Database.SPECIES_COLUMN_ID + " = ?";

    public static final String COUNT_ALL_SPECIES = "SELECT COUNT(*) FROM " + Database.TABLE_SPECIES;

    public static final String SELECT_MAX_USED_ID = "SELECT MAX(" + Database.SPECIES_COLUMN_ID + ") "
            + "FROM " + Database.TABLE_SPECIES;

    /**
     * Singleton instance of class.
     */
    private static SpeciesDatabase instance = new SpeciesDatabase();
    /**
     * Last (highest number) used ID in database.
     */
    private int lastUsedId;
    /**
     * List of all species in shelter.
     */
    private ObservableList<Species> speciesList;

    /**
     * Class constructor.
     */
    public SpeciesDatabase() {
        lastUsedId = selectLastUsedId();
        speciesList = FXCollections.observableList(getAllSpeciesFromDatabase());
    }


    public ObservableList<Species> getSpeciesList() {
        return speciesList;
    }

    /**
     * Method connects to database and loads every record from SPECIES table.
     * Each record from database is then used to create one {@link Species} object.
     *
     * @return List of {@link Species} objects loaded from database.
     */
    public static List<Species> getAllSpeciesFromDatabase() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_SPECIES)) {

            List<Species> toReturn = new LinkedList<>();
            if (resultSet.next()) {
                int idColumn = resultSet.findColumn(Database.SPECIES_COLUMN_ID);
                int nameColumn = resultSet.findColumn(Database.SPECIES_COLUMN_NAME);
                int limitColumn = resultSet.findColumn(Database.SPECIES_COLUMN_LIMIT);

                do {
                    int id = resultSet.getInt(idColumn);
                    String name = resultSet.getString(nameColumn);
                    int limit = resultSet.getInt(limitColumn);
                    int occupiedPlaces = AnimalDatabase.countAnimalOfSpecifiedSpecies(id);

                    toReturn.add(new Species(id, name, limit, occupiedPlaces));
                } while (resultSet.next());
            }

            return toReturn;

        } catch (SQLException e) {
            e.printStackTrace();
            return new LinkedList<>();
        }
    }

    /**
     * Creates new {@link Species} object and adds it to database.
     *
     * @param name Species name
     * @param limit Limit of places for the species
     * @return Added {@link Species} object if succeeded; false otherwise
     */
    public Species addNewSpecies(String name, int limit) {
        if (checkIfNameExistsInDatabase(name)) {
            return null;
        }

        Species toAdd = new Species(++lastUsedId, name, limit);

        if (insertNewSpecies(toAdd) && speciesList.add(toAdd)) {
            return toAdd;
        }

        return null;
    }

    /**
     * Changes places limit for passed {@link Species}
     *
     * @param speciesId ID of species to be changed
     * @param newLimits New value of limit
     * @return true if succeeded; false if object not found, value of
     * limit wrong or any other error
     */
    public boolean changeSpeciesPlacesLimits(int speciesId, int newLimits) {
        if (newLimits < 1) {
            return false;
        }

        Species species = getSpeciesById(speciesId);

        if (species == null) {
            return false;
        }

        if (updateLimits(speciesId, newLimits)) {
            species.setPlacesLimit(newLimits);
            return true;
        }

        return false;
    }

    /**
     * Connects to database and updates limit of places in record containing
     * info about {@link Species} object with ID as passed.
     *
     * @param id ID of species to be changed
     * @param newLimits New value of limit
     * @return true if succeeded; false if query result failed
     * or SQLException occurred
     */
    private static boolean updateLimits(int id, int newLimits) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_LIMITS)) {

            preparedStatement.setInt(1, newLimits);
            preparedStatement.setInt(2, id);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Connects to database and inserts passed
     * {@link Species} object into it.
     *
     * @param newSpecies {@link Species} object to insert into database
     * @return true if succeeded; false if query result failed
     * or SQLException occurred
     */
    private boolean insertNewSpecies(Species newSpecies) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_SPECIES)) {

            preparedStatement.setString(1, newSpecies.getName());
            preparedStatement.setInt(2, newSpecies.getPlacesLimit());

            return preparedStatement.executeUpdate() == 1;

        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Connects to database and counts number of records in ANIMALS table.
     *
     * @return Number of animals in database. -1 in case of SQLException;
     * -2 if query result failed
     */
    public static int countAllSpecies() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(COUNT_ALL_SPECIES)) {

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

    /**
     * Checks if {@link Species} object with field {@link Species#name}
     * same as passed String is in {@link #speciesList}
     *
     * @param name Name to check
     * @return true if object with such {@link Species#name} exists; false otherwise
     */
    private boolean checkIfNameExistsInDatabase(String name) {
        return speciesList.stream()
                .map(Species::getName)
                .anyMatch(c -> c.equals(name));
    }

    /**
     * Connects to database and loads ID of last record in SPECIES table.
     *
     * @return ID of last species; -1 in case of SQLException
     * or -2 if query result failed
     */
    private static int selectLastUsedId() {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_MAX_USED_ID)) {


            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return -2;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }


    public static SpeciesDatabase getInstance() {
        return instance;
    }

    /**
     * Search {@link #speciesList} for object with {@link Species#id} field
     * same as passed number.
     *
     * @param speciesId ID to search for
     * @return {@link Species} object if exists;
     * null if there is no object with such ID
     */
    public Species getSpeciesById(int speciesId) {
        Optional<Species> toReturn = speciesList
                .stream()
                .filter(c -> c.getId() == speciesId)
                .findAny();

        if (toReturn.isPresent()) {
            return toReturn.get();
        } else {
            return null;
        }
    }
}
