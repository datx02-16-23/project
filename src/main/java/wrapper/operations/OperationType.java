package wrapper.operations;

import javafx.scene.paint.Color;

/**
 * The name of the operation.
 */
public enum OperationType{
    message(null), read(Color.GREEN), write(Color.RED), swap(true, Color.TEAL), remove(Color.GREY);

    /**
     * True for operations which implement the Consolidable interface.
     */
    public final boolean consolidable;
    
    public final Color color;

    OperationType (boolean consolidable, Color color){
        this.consolidable = consolidable;
        this.color = color;
    }
    
    OperationType (Color color){
        this(false, color);
    }
}
