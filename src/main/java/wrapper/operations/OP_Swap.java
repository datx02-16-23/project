package wrapper.operations;

import java.util.HashMap;
import java.util.List;

import application.assets.Strings;
import wrapper.Locator;
import wrapper.Operation;

/**
 * Create a new Swap operation, shifting the values of {@code var1} and
 * {@code var2}.
 */
public class OP_Swap extends Operation implements Consolidable {

	/**
	 * Version number for this class.
	 */
	private static final long serialVersionUID = Strings.VERSION_NUMBER;

	/**
	 * Create a new Swap operation. Note that you must set var1, var2, and
	 * value.
	 */
	public OP_Swap() {
		super(OperationType.swap, new HashMap<Key, Object>(), null, -1, -1, -1, -1);
	}

	/**
	 * Set var1 for this Swap operation. The identifier of the variable should
	 * be previously declared in the header.
	 * 
	 * @param var1
	 *            Variable 1 for this Swap operation.
	 */
	public void setVar1(Locator var1) {
		this.operationBody.put(Key.var1, var1);
	}

	/**
	 * Set var2 for this Swap operation. The identifier of the variable should
	 * be previously declared in the header.
	 * 
	 * @param var2
	 *            Variable 2 for this Swap operation.
	 */
	public void setVar2(Locator var2) {
		this.operationBody.put(Key.var2, var2);
	}

	/**
	 * The values contained at var1 and var2 respectively, AFTER this Swap
	 * operation has been executed.
	 * 
	 * @param values
	 *            The values in var1 and var2 after execution.
	 */
	public void setValues(double[] values) {
		this.operationBody.put(Key.value, values);
	}

	public Locator getVar1() {
		return (Locator) this.operationBody.get(Key.var1);
	}

	public Locator getVar2() {
		return (Locator) this.operationBody.get(Key.var2);
	}

	public double[] getValue() {
		return (double[]) this.operationBody.get(Key.value);
	}

	@Override
	public String toString() {
		return "SWAP: " + getVar1().toString() + " <-> " + getVar2().toString();
	}

	/**
	 * Attempt to create a Swap operation from 3 read/write operations.
	 * 
	 * @param rwList
	 *            The list of 3 read/write operations to test.
	 * @return A new Swap operation if the given testSet is a valid
	 *         decomposition of a Swap operation, null otherwise.
	 */
	@Override
	public Operation consolidate(List<OP_ReadWrite> rwList) {
		if (rwList.size() != 3) {
			throw new IllegalArgumentException("Swap operations are composed of 3 read/write operations.");
		}
		OP_ReadWrite rw0 = rwList.get(0);
		OP_ReadWrite rw1 = rwList.get(1);
		OP_ReadWrite rw2 = rwList.get(2);

//		Locator op0_source = rw0.getSource();
//		Locator op0_target = rw0.getTarget();
		
//		Locator op1_source = rw1.getSource();
//		Locator op1_target = rw1.getTarget();
		
//		Locator op2_source = rw2.getSource();
//		Locator op2_target = rw2.getTarget();
		
		if (rw0.getSource() == null || rw0.getTarget() == null ||
			rw1.getSource() == null || rw1.getTarget() == null ||
			rw2.getSource() == null || rw2.getTarget() == null)
		{
			return null; // All sources/targets must be known.
		}
		Locator var1, tmp, var2;
		// Operation 1: Set var1 -> tmp
		var1 = rw0.getSource();
		tmp = rw0.getTarget();
		if (tmp.index != null) {
			return null; // tmp should not be another array.
		}
		// Operation 2: x -> var1?
		if (rw1.getTarget().equals(var1)) {
			var2 = rw1.getSource(); // Set x = var2
		} else {
			return null;
		}
		// Operation 3: tmp -> var2?
		if (!(rw2.getSource().equals(tmp) && rw2.getTarget().equals(var2))) {
			return null;
		}
		// Construct and return swap operation.
		OP_Swap op_swap = new OP_Swap();
		op_swap.setVar1(var1);
		op_swap.setVar2(var2);
		op_swap.setValues(new double[] { rw1.getValue()[0], rw0.getValue()[0] });
		return op_swap;
	}

	@Override
	public int getRWcount() {
		return 3;
	}

	@Override
	public String toSimpleString() {
		return toString();
	}
}
