package wrapper.operations;

import java.util.List;

import wrapper.Operation;

/**
 * A consolidable element can be broken into a series read/write operations.
 * <br>
 * <b>IMPORTANT:</b> Any class implementing Consolidable should inherit
 * Operation!
 * 
 * @author Richard Sundqvist
 *
 */
public interface Consolidable {

	/**
	 * Attempt to consolidate a list of read/write operations. The method should
	 * behave as it were static, that is the state of the calling initialization
	 * should not matter.
	 * 
	 * @param rwList
	 *            The list to attempt consolidation on.
	 * @return A high level operation if the supplied list could be
	 *         consolidated, null otherwise.
	 */
	public Operation consolidate(List<OP_ReadWrite> rwList);

	/**
	 * Returns the number of primitive (read/write) operations this high level
	 * operation consists of. The method should behave as it were static, that
	 * is the state of the calling initialization should not matter.
	 * 
	 * @return The number of primitive (read/write) operations this high level
	 *         operation consists of.
	 */
	public int getRWcount();
}
