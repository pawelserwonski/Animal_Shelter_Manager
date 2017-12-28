package ski.serwon.AnimalShelterManager.model;

import java.util.List;

/**
 * Class represents either species in case of 'other' (undefined)
 * animal or more accurately breed in case of one of defined type
 * of animal.
 *
 * @author Paweł Serwoński
 * @version 1.0
 * @since 1.0
 */
public class Breed {
    private String name;
    private boolean requireWalk;
    private int id;

    public Breed(String name, boolean requireWalk) {
        this.name = name;
        this.requireWalk = requireWalk;
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
