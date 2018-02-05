package ski.serwon.AnimalShelterManager.model.datamodel;

public class ApplicationSettings {
    public static int percentOfFreePlacesToWarning = 10;

    public static int getPercentOfFreePlacesToWarning() {
        return percentOfFreePlacesToWarning;
    }

    public static void setPercentOfFreePlacesToWarning(int percentOfFreePlacesToWarning) {
        ApplicationSettings.percentOfFreePlacesToWarning = percentOfFreePlacesToWarning;
    }
}
