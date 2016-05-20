package contract.operation;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * The name of the operation.
 */
public enum OperationType {
	read(Color.GREEN), write(Color.RED), swap(true, Color.CORNFLOWERBLUE), remove(Color.HOTPINK), message(Color.AQUA);

	/**
	 * True for operations which implement the Consolidable interface.
	 */
	public final boolean consolidable;

	public final Color color;

	OperationType(boolean consolidable, Color color) {
		this.consolidable = consolidable;
		this.color = color;
	}

	OperationType(Color color) {
		this(false, color);
	}
}
