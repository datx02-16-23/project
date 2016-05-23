package multiset.model;

import java.util.List;

/**
 * Your bouncing model model must adhere to this interface in order to make use
 * of the pre-written classes for drawing the model.
 *
 * @author Oscar Soderlund
 *
 */
public interface iModel {
    /**
     * Returns a list of shape representations of the model. Used by the
     * multiset.model.BouncingBalls class to draw the model.
     *
     * @return the model as shape objects
     */
    public List<Ball> getBalls ();

    /**
     * Changes the state of the model using the Euler method by simulating
     * deltaT units of time.
     *
     * @param deltaT
     *            the amount of time to simulate
     */
    public void tick (double deltaT);
}
