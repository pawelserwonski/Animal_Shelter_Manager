package ski.serwon.AnimalShelterManager.model;

import java.util.List;

/**
 *
 * @author Paweł Serwoński
 * @version 1.0
 * @since 1.0
 */
public class Breed {
    private String name;
    private boolean requireWalk;
    private int id;

    public Breed(String name, boolean requireWalk, int id) {
        this.name = name;
        this.requireWalk = requireWalk;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public boolean doesRequireWalk() {
        return requireWalk;
    }

    public int getId() {
        return id;
    }
}
