package model2;

import java.util.List;

import contract.json.Operation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

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
    public final ExecutionModel    executionModel;

    /**
     * Time line used for timed model progression.
     */
    private final Timeline         autoExecutionTimeline;

    /**
     * The model execution listener for the controller.
     */
    private ModelExecutionListener modelExecutionListener;

    // ============================================================= //
    /*
     *
     * Constructors
     *
     */
    // ============================================================= //

    /**
     * Create a new model controller.
     * 
     * @param executionModel
     *            The model to control.
     */
    public ModelController (ExecutionModel executionModel) {
        this.executionModel = executionModel;

        // Auto execution timeline
        autoExecutionTimeline = new Timeline();
        autoExecutionTimeline.setAutoReverse(false);
        autoExecutionTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Create a new model controller for {@link ExecutionModel#INSTANCE}.
     * 
     */
    public ModelController () {
        this(ExecutionModel.INSTANCE);
    }

    // ============================================================= //
    /*
     *
     * Control - added functionality
     *
     */
    // ============================================================= //

    /**
     * Begin timed execution for the model.
     * 
     * @param millis
     *            The time between executions.
     */
    public void startAutoExecution (long millis) {
        autoExecutionTimeline.getKeyFrames().clear();

        KeyFrame executionFrame = new KeyFrame(Duration.millis(millis), event -> {
            if (executionModel.tryExecuteNext()) {
                modelExecutionListener.operationsExecuted(executionModel.executeNext());
            } else {
                stopAutoExecution();
            }
        });

        autoExecutionTimeline.getKeyFrames().add(executionFrame);
        autoExecutionTimeline.play();
    }

    /**
     * Stop timed execution for the model.
     */
    public void stopAutoExecution () {
        autoExecutionTimeline.stop();
    }
    
    // ============================================================= //
    /*
     *
     * Control - convenience
     *
     */
    // ============================================================= //

}
