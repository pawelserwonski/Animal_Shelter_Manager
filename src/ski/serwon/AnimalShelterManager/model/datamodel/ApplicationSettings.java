package ski.serwon.AnimalShelterManager.model.datamodel;

/**
 * Set of variables designed to set the application.
 *
 * @version 1.0
 * @since 1.0
 */
public class ApplicationSettings {
    /**
     * If percentage of free places in any species is less than set,
     * warning should be shown.
     */
    public static int percentOfFreePlacesToWarning = 10;

    public static int getPercentOfFreePlacesToWarning() {
        return percentOfFreePlacesToWarning;
    }

    public static void setPercentOfFreePlacesToWarning(int percentOfFreePlacesToWarning) {
        ApplicationSettings.percentOfFreePlacesToWarning = percentOfFreePlacesToWarning;
    }
}
