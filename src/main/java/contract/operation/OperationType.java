package contract.operation;

import javafx.scene.paint.Color;

/**
 * The name of the operation.
 */
public enum OperationType {
    read(Color.GREEN), write(Color.RED), message(Color.AQUA), // Standard
    remove(null), // Atomic, non-standard
    swap(true, Color.CORNFLOWERBLUE); // Non-atomic

    /**
     * True for operations which implement the Consolidable interface.
     */
    public final boolean consolidable;

    public final Color   color;

    OperationType (boolean consolidable, Color color) {
        this.consolidable = consolidable;
        this.color = color;
    }

    OperationType (Color color) {
        this(false, color);
    }
}
