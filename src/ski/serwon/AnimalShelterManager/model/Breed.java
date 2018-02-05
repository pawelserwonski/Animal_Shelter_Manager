package ski.serwon.AnimalShelterManager.model;

import java.util.List;

/**
 * Class represents certain breed i.e. Labrador, British etc.
 *
 * @author Pawel Serwonski
 * @version 1.0
 * @since 1.0
 */
public class Breed {
    /**
     * Breed's name.
     */
    private String name;

    /**
     * Does {@link Animal} of certain Breed requires to be walked out.
     */
    private boolean requireWalk;

    /**
     * ID in database.
     */
    private int id;

    /**
     * Class constructor.
     * @param name Breed's name.
     * @param requireWalk Does {@link Animal} of certain Breed requires to be walked out.
     * @param id ID in database.
     */
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

    public void setRequireWalk(boolean requireWalk) {
        this.requireWalk = requireWalk;
    }
}
