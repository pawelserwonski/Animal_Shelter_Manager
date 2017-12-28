package ski.serwon.AnimalShelterManager.model.datamodel;

import java.io.File;
import java.sql.Connection;

public class Database {
    public static final String DB_NAME = "database.db";
    public static final String CONNECTION_STRING = "jdbc:sqlite:." + File.separator + "data" + File.separator + DB_NAME;


    public static final String TABLE_ANIMALS = "animals";
    public static final String ANIMALS_COLUMN_ID = "_id";
    public static final String ANIMALS_COLUMN_BREED = "breed";
    public static final String ANIMALS_COLUMN_NAME = "name";
    public static final String ANIMALS_COLUMN_SEX = "sex";
    public static final String ANIMALS_COLUMN_BIRTHDATE = "birthdate";
    public static final String ANIMALS_COLUMN_IN_SHELTER_SINCE = "inShelterSince";
    public static final String ANIMALS_COLUMN_LAST_WALK = "lastWalk";


    public static final String TABLE_BREEDS = "breeds";
    public static final String BREEDS_COLUMN_ID = "_id";
    public static final String BREEDS_COLUMN_SPECIES = "species";
    public static final String BREEDS_COLUMN_NAME = "name";
    public static final String BREEDS_COLUMN_REQUIRE_WALKS = "requireWalks";


    public static final String TABLE_SPECIES = "species";
    public static final String SPECIES_COLUMN_ID = "_id";
    public static final String SPECIES_COLUMN_NAME = "name";
    public static final String SPECIES_COLUMN_LIMIT = "limit";


}
