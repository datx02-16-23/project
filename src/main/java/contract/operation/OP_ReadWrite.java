package contract.operation;

import java.util.Arrays;
import java.util.HashMap;

import assets.DasConstants;
import contract.Locator;
import contract.Operation;

/**
 * A primitive operation from which all other operations on data structures may
 * be constructed.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class OP_ReadWrite extends Operation {

	/**
	 * Version number for this class.
	 */
	private static final long serialVersionUID = DasConstants.VERSION_NUMBER;

	/**
	 * Create a new ReadWrite operation. Note that you must set the target,
	 * source and value.
	 * 
	 * @param operation
	 *            The name of the operation. Should be "read" or "write".
	 */
	public OP_ReadWrite(OperationType operation) {
		super(operation, new HashMap<Key, Object>(), null, -1, -1, -1, -1);
	}

	public OP_ReadWrite(OperationType operation, String source, int beginLine, int endLine, int beginColumn,
			int endColumn) {
		super(operation, new HashMap<Key, Object>(), source, beginLine, endLine, beginColumn, endColumn);
	}

	/**
	 * Set the target variable for this ReadWrite operation. The identifier of
	 * the variable should be previously declared in the header.
	 * 
	 * @param target
	 *            The target variable for this ReadWrite operation.
	 */
	public void setTarget(Locator target) {
		this.operationBody.put(Key.target, target);
	}

	/**
	 * Set the source variable for this ReadWrite operation. The identifier of
	 * the variable should be previously declared in the header.
	 * 
	 * @param source
	 *            The source variable for this ReadWrite operation.
	 */
	public void setSource(Locator source) {
		this.operationBody.put(Key.source, source);
	}

	/**
	 * Set the value(s) which were ReadWrite from {@code source}. This should be
	 * the value of {@code target} and the specified index after operation
	 * execution, if applicable.
	 * 
	 * @param value
	 *            Set the value(s) which were ReadWrite from {@code source}.
	 */
	public void setValue(double[] value) {
		this.operationBody.put(Key.value, value);
	}

	@Override
	public String toString() {
		Locator source = getSource();
		Locator target = getTarget();
		String sourceStr;
		String targetStr;
		// Source and target known
		if (source != null && target != null) {
			sourceStr = source.toString();
			targetStr = target.toString();
			// Assume source or target known.
		} else {
			double[] value = getValue();
			String valueStr = value == null ? "?" : Arrays.toString(value);
			// Source unknown
			if (source == null) {
				sourceStr = valueStr;
				targetStr = target.toString();
				// target <-- values
				return super.operation.toString().toUpperCase() + ": " + targetStr + " <-- " + sourceStr;
			}
			// Target unknown
			else {
				sourceStr = source.toString();
				targetStr = valueStr;
			}
		}
		// source <-- target || values
		return super.operation.toString().toUpperCase() + ": " + sourceStr + " --> " + targetStr;
	}

	public Locator getTarget() {
		return (Locator) this.operationBody.get(Key.target);
	}

	public Locator getSource() {
		return (Locator) this.operationBody.get(Key.source);
	}

	public double[] getValue() {
		return (double[]) this.operationBody.get(Key.value);
	}
}
