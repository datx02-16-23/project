package contract;

import java.io.Serializable;
import java.util.Arrays;

import assets.Const;
import assets.Tools;

/**
 * A variable used by the {@code Operation} class.
 */
public class Locator implements Serializable {

    /**
     * Version number for this class.
     */
    private static final long serialVersionUID = Const.VERSION_NUMBER;
    /**
     * The identifier for this Locator. Will generally match one of the identifiers used as keys in
     * the {@code annotatedVariables} HashMap of the variables declared in the header.
     */
    public final String       identifier;
    /**
     * The index from which the fetch a value in the variable identified by {@code identifier}.
     */
    public final int[]        index;

    /**
     * Create a new Locator with a given identifier and index.
     *
     * @param identifier
     *            The identifier for this variable.
     * @param index
     *            The index from which the fetch a value in the variable identified by
     *            {@code identifier}.
     */
    public Locator (String identifier, int[] index) {
        this.identifier = identifier;
        this.index = index;
    }

    @Override public String toString () {
        return Tools.stripQualifiers(identifier) + (index == null ? "" : Arrays.toString(index));
    }

    @Override public boolean equals (Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Locator == false) {
            return false;
        }
        Locator rhs = (Locator) other;
        return identifier.equals(rhs.identifier) && Arrays.equals(index, rhs.index);
    }

    /**
     * Compares the index only, ignoring identifier.
     *
     * @param other
     *            The other Locator to compare.
     * @return True if the index of this and other are equal, false otherwise.
     */
    public boolean indexEquals (Locator other) {
        return Arrays.equals(index, other.index);
    }

    /**
     * Compares the index only, ignoring identifier.
     *
     * @param other
     *            The index to compare.
     * @return True if the index of this and other are equal, false otherwise.
     */
    public boolean indexEquals (int[] other) {
        return Arrays.equals(index, other);
    }
}
