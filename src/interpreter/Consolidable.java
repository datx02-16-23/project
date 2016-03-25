package interpreter;

import java.util.List;

import manager.operations.OP_ReadWrite;
import wrapper.Operation;

public interface Consolidable {
	/**
	 * Consolidate
	 * @param rwList
	 * @return
	 */
	public Operation consolidate(List<OP_ReadWrite> rwList);
	/**
	 * Returns the number of primitive (read/write) operations this high level operation consists of.
	 * @return The number of primitive (read/write) operations this high level operation consists of.
	 */
	public int getRWcount();
}
