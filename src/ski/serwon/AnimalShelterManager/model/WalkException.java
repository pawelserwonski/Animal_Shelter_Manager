package ski.serwon.AnimalShelterManager.model;

/**
 * Exception occurs when data about walks history
 * is tried to be retrieved for animal which
 * should not be walked out.
 *
 * @author Paweł Serwoński
 * @version 1.0
 * @since 1.0
 */
public class WalkException extends Exception {
    public WalkException(String message) {
        super(message);
    }
}
