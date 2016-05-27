package model;

/**
 * 
 * @author Richard Sundqvist
 */
public interface ExecutionTickListener {

    /**
     * Called when the current tick updates.
     * 
     * @param tickNumber
     *            The current tick number.
     */
    public void tickUpdate (int tickNumber);
}
