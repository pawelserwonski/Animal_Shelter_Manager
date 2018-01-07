package ski.serwon.AnimalShelterManager.model.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ski.serwon.AnimalShelterManager.model.Species;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SpeciesDatabase {
    private static SpeciesDatabase instance = new SpeciesDatabase();
    private int lastUsedId;
    private ObservableList<Species> speciesList;


    public SpeciesDatabase() {
        lastUsedId = selectLastUsedId();
        speciesList = FXCollections.observableList(selectAllSpecies());
    }

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

    public ObservableList<Species> getSpeciesList() {
        return speciesList;
    }

    public static List<Species> selectAllSpecies() {
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

                    toReturn.add(new Species(id, name, limit));
                } while (resultSet.next());
            }

            return toReturn;

        } catch (SQLException e) {
            e.printStackTrace();
            return new LinkedList<>();
        }
    }

    public Species addNewSpecies(String name, int limit) {
        if (checkIfNameExistsInDatabase(name)) {
            return null;
        }

        Species toAdd = new Species(++lastUsedId, name, limit);
        if (speciesList.add(toAdd)) {
            if (insertNewSpecies(toAdd)) {
                return toAdd;
            } else {
                speciesList.remove(toAdd);
                return null;
            }
        }
        return null;
    }

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

    private static boolean updateLimits(int id, int newLimits) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_LIMITS)) {

            preparedStatement.setInt(1, newLimits);
            preparedStatement.setInt(2, id);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            //todo
            return false;
        }
    }

    private boolean insertNewSpecies(Species newSpecies) {
        try (Connection connection = DriverManager.getConnection(Database.CONNECTION_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_SPECIES)) {

            preparedStatement.setString(1, newSpecies.getName());
            preparedStatement.setInt(2, newSpecies.getPlacesLimit());

            return preparedStatement.executeUpdate() == 1;

        } catch (SQLException e) {
            //todo
            return false;
        }
    }

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

    private boolean checkIfNameExistsInDatabase(String name) {
        return speciesList.stream()
                .map(Species::getName)
                .anyMatch(c -> c.equals(name));
    }

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
