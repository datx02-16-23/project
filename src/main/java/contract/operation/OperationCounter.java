package contract.operation;

import gui.Main;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * A class which counts performed operations.
 *
 * @author Richard Sundqvist
 *
 */
public class OperationCounter {

    private final SimpleIntegerProperty read    = new SimpleIntegerProperty(0);
    private final SimpleIntegerProperty write   = new SimpleIntegerProperty(0);
    private final SimpleIntegerProperty swap    = new SimpleIntegerProperty(0);
    private final SimpleIntegerProperty remove  = new SimpleIntegerProperty(0);
    private final SimpleIntegerProperty message = new SimpleIntegerProperty(0);

    /**
     * @return The reads property.
     */
    public SimpleIntegerProperty readsProperty () {
        return this.read;
    }

    /**
     * Returns the the Writes property.
     *
     * @return the Writes property.
     */
    public SimpleIntegerProperty writesProperty () {
        return this.write;
    }

    /**
     * Returns the the Swaps property.
     *
     * @return the Swaps property.
     */
    public SimpleIntegerProperty swapsProperty () {
        return this.swap;
    }

    /**
     * Returns the the Removes property.
     *
     * @return the Removes property.
     */
    public SimpleIntegerProperty removesProperty () {
        return this.remove;
    }

    /**
     * Returns the Messages property.
     *
     * @return the Messages property.
     */
    public SimpleIntegerProperty messagesProperty () {
        return this.message;
    }

    /**
     * Count an operation. Calls {@code countOperation(OperationType type)}.
     *
     * @param op
     *            The operation to count.
     */
    public void count (OperationType op) {
        this.countOperation(op);
    }

    /**
     * Count an operation type.
     *
     * @param type
     *            The count to count.
     */
    public void countOperation (OperationType type) {
        switch (type) {
        case message:
            this.message.setValue(this.message.getValue() + 1);
            break;
        case read:
            this.read.setValue(this.read.getValue() + 1);
            break;
        case remove:
            this.remove.setValue(this.remove.getValue() + 1);
            break;
        case swap:
            this.swap.setValue(this.swap.getValue() + 1);
            break;
        case write:
            this.write.setValue(this.write.getValue() + 1);
            break;
        default:
            break;

        }
    }

    /**
     * Returns the number of Read operations.
     *
     * @return The number of Read operations.
     */
    public int getReads () {
        return this.read.get();
    }

    /**
     * Returns the number of Writes operations.
     *
     * @return The number of Writes operations.
     */
    public int getWrites () {
        return this.write.get();
    }

    /**
     * Returns the number of Swap operations.
     *
     * @return The number of Swap operations.
     */
    public int getSwap () {
        return this.swap.get();
    }

    /**
     * Returns the number of Remove operations.
     *
     * @return The number of Remove operations.
     */
    public int getRemove () {
        return this.remove.get();
    }

    /**
     * Returns the number of Message operations.
     *
     * @return The number of Message operations.
     */
    public int getMessage () {
        return this.message.get();
    }

    /**
     * Reset the counter.
     */
    public void reset () {
        this.read.set(0);
        this.write.set(0);
        this.swap.set(0);
        this.remove.set(0);
        this.message.set(0);
    }

    /**
     * Print the recorded statistics using the {@link #GUIConsole}.
     */
    public void printStats () {
        Main.console.info("Read: " + this.read.intValue());
        Main.console.info("Write: " + this.write.intValue());
        Main.console.info("Swap: " + this.swap.intValue());
        // Main.console.info("Remove: " + remove.intValue());
        // Main.console.info("Message: " + message.intValue());
    }

    /**
     * Interface for classes which count the {@link #Operation}'s performed on
     * it.
     *
     * @author Richard Sundqvist
     *
     */
    public interface OperationCounterHaver {
        /**
         * Returns the OperationCounter for this OperationCounterHaver.
         *
         * @return An OperationCounter
         */
        public OperationCounter getCounter ();

        /**
         * Print stats for a OperationCounterHaver using the
         * {@link #printStats()} method.
         *
         * @param och
         *            The OperationCounterHaver whose counter should be used.
         */
        public static void printStats (OperationCounterHaver och) {
            och.getCounter().printStats();
        }
    }
}
