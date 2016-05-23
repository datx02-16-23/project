package interpreter;

import java.util.ArrayList;
import java.util.List;

import contract.Operation;
import contract.operation.Consolidable;
import contract.operation.OP_ReadWrite;
import contract.operation.OP_Swap;
import contract.operation.OperationType;

/**
 * A Consolidator attempts to consolidate low level (read/write) operations into
 * higher level operations.
 * 
 * @author Richard Sundqvist
 *
 */
public class Consolidator {

    /**
     * The maximum number of operations a Consolidable may consist of.
     */
    public static final int                 MAX_SIZE       = 100;
    private int                             minimumSetSize = Integer.MAX_VALUE;
    private int                             maximumSetSize = Integer.MIN_VALUE;
    private final ArrayList<Consolidable>[] invokers;

    /**
     * Create a new Consolidator with the default types.
     */
    @SuppressWarnings("unchecked")
    public Consolidator () {
        this.invokers = new ArrayList[MAX_SIZE];
        for (int i = 0; i < MAX_SIZE; i++) {
            this.invokers [i] = new ArrayList<Consolidable>();
        }
        this.addDefaultInvokers();
    }

    /**
     * Add the default invokers to the list of possible consolidatons.
     */
    private void addDefaultInvokers () {
        this.addConsolidable(new OP_Swap());
    }

    /**
     * Returns true if the type of the OperationType given as argument is among
     * the test cases used by this Consolidator.
     * 
     * @param testCase
     *            The OperationType to test.
     * @return True if the test case type is among the tested, false otherwise.
     */
    public boolean checkTestCase (OperationType testCase) {
        List<OperationType> testCases = this.getTestCases();
        for (OperationType ot : testCases) {
            if (ot == testCase) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds another Consolidable for this Consolidator. Will not any any
     * Consolidable type more than once.
     * 
     * @param newConsolidable
     *            The Consolidable to add.
     */
    public void addConsolidable (Consolidable newConsolidable) {
        int rwCount = newConsolidable.getRWcount();
        if (rwCount < this.minimumSetSize) {
            this.minimumSetSize = rwCount;
        }
        if (rwCount > this.maximumSetSize) {
            this.maximumSetSize = rwCount;
        }
        Operation newOp = (Operation) newConsolidable;
        for (Consolidable c : this.invokers [rwCount]) {
            Operation op = (Operation) c;
            if (newOp.operation == op.operation) {
                return;
            }
        }
        this.invokers [rwCount].add(newConsolidable);
    }

    /**
     * Attempt to consolidate the working set supplied. Returns a consolidated
     * operation if successful, null otherwise.
     * 
     * @param rwList
     *            The list of read/write operations to consolidate.
     * @return A consolidated operation if successful, null otherwise.
     */
    public Operation attemptConsolidate (List<OP_ReadWrite> rwList) {
        Operation consolidatedOperation = null;
        for (Consolidable c : this.invokers [rwList.size()]) {
            consolidatedOperation = c.consolidate(rwList);
            if (consolidatedOperation != null) {
                return consolidatedOperation;
            }
        }
        return null;
    }

    /**
     * Returns the maximum number of read/write operations this Consolidator
     * will use.
     * 
     * @return The maximum number of read/write operations this Consolidator
     *         will use.
     */
    public int getMaximumSetSize () {
        if (this.minimumSetSize == Integer.MIN_VALUE) {
            return -1;
        }
        return this.maximumSetSize;
    }

    /**
     * Returns the minimum number of read/write operations this Consolidator
     * will use.
     * 
     * @return The minimum number of read/write operations this Consolidator
     *         will use.
     */
    public int getMinimumSetSize () {
        if (this.minimumSetSize == Integer.MAX_VALUE) {
            return -1;
        }
        return this.minimumSetSize;
    }

    /**
     * Print a human-readable list of all the Operation types this Consolidator
     * tests.
     * 
     * @return A human-readable list of all the Operation types this
     *         Consolidator tests.
     */
    public List<OperationType> getTestCases () {
        ArrayList<OperationType> simpleNames = new ArrayList<OperationType>();
        for (int i = 0; i < MAX_SIZE; i++) {
            for (Consolidable c : this.invokers [i]) {
                Operation op = (Operation) c;
                simpleNames.add(op.operation);
            }
        }
        return simpleNames;
    }

    /**
     * Remove a given testCase. When this method returns, the testcase is
     * guaranteed to be removed.
     * 
     * @param testCase
     *            The testcase to remove.
     * @param rwCount
     *            The number of variables the test case consists of.
     */
    public void removeTestCase (OperationType testCase, int rwCount) {
        Consolidable victim = null;
        for (Consolidable c : this.invokers [rwCount]) {
            Operation op = (Operation) c;
            if (testCase == op.operation) {
                victim = c;
                break;
            }
        }
        if (victim != null) {
            this.invokers [rwCount].remove(victim);
        }
        if (victim.getRWcount() == this.maximumSetSize) {
            this.recalculateMaxSetSize();
        }
        if (victim.getRWcount() == this.minimumSetSize) {
            this.recalculateMinSetSize();
        }
    }

    /**
     * Recalculate the maximum set size used by the Consolidator.
     */
    private void recalculateMaxSetSize () {
        this.maximumSetSize = Integer.MIN_VALUE;
        for (int i = 0; i < MAX_SIZE; i++) {
            for (Consolidable c : this.invokers [i]) {
                if (c.getRWcount() > this.maximumSetSize) {
                    this.maximumSetSize = c.getRWcount();
                }
            }
        }
    }

    /**
     * Recalculate the minimum set size used by the Consolidator.
     */
    private void recalculateMinSetSize () {
        this.minimumSetSize = Integer.MAX_VALUE;
        for (int i = 0; i < MAX_SIZE; i++) {
            for (Consolidable c : this.invokers [i]) {
                if (c.getRWcount() > this.minimumSetSize) {
                    this.minimumSetSize = c.getRWcount();
                }
            }
        }
    }
}
