package model2;

import javafx.animation.Timeline;

/**
 * ExecutionModel convenience class.
 * 
 * @author Richard Sundqvist
 *
 */
public class ModelController {

    // ============================================================= //
    /*
     *
     * Field variables
     *
     */
    // ============================================================= //

    /**
     * The model this controller is responsible for.
     */
    public final ExecutionModel executionModel;

    private final Timeline      autoExecutionTimeline;

    /**
     * Create a new model controller.
     * 
     * @param executionModel
     *            The model to control.
     */
    public ModelController (ExecutionModel executionModel) {
        this.executionModel = executionModel;
        autoExecutionTimeline = new Timeline();
    }

    /**
     * Create a new model controller for {@link ExecutionModel#INSTANCE}.
     * 
     */
    public ModelController () {
        this(ExecutionModel.INSTANCE);
    }

    /**
     * Begin timed execution the model.
     */
    public void start () {
        
    }

    public void stop () {
        autoExecutionTimeline.stop();
    }
}
