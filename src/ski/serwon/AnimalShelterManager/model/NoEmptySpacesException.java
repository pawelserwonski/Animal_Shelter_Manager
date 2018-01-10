package ski.serwon.AnimalShelterManager.model;

public class NoEmptySpacesException extends Exception {
    public NoEmptySpacesException() {
        super();
    }

    public NoEmptySpacesException(String message) {
        super(message);
    }
}
