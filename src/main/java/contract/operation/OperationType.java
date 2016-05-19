package contract.operation;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * The name of the operation.
 */
public enum OperationType {
	message(null), read(Color.GREEN), write(Color.RED), swap(true, Color.CORNFLOWERBLUE), remove(Color.HOTPINK);

	/**
	 * True for operations which implement the Consolidable interface.
	 */
	public final boolean consolidable;

	public final Paint paint;

	OperationType(boolean consolidable, Paint paint) {
		this.consolidable = consolidable;
		this.paint = paint;
	}

	OperationType(Paint paint) {
		this(false, paint);
	}
}
