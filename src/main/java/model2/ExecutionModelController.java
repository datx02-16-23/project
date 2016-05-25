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
public class ExecutionModelController {

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
    public final ExecutionModel        executionModel;

    /**
     * Time line used for timed model progression.
     */
    private final Timeline             autoExecutionTimeline;

    /**
     * The execution listener for the controller.
     */
    private OperationsExecutedListener operationsExecutedListener;

    /**
     * Time line used for timed model progression.
     */
    private final Timeline             executionTickTimeline;

    /**
     * The number of ticks per execution call. That is, the number of times the the
     * {@code executionTickListener} will be called for each time the
     * {@code operationsExecutedListener} will be called.
     */
    private int                        tickCount;

    /**
     * The tick listener for the controller.
     */
    private ExecutionTickListener      executionTickListener;

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
    public ExecutionModelController (ExecutionModel executionModel) {
        this.executionModel = executionModel;

        // Auto execution timeline.
        autoExecutionTimeline = new Timeline();
        autoExecutionTimeline.setAutoReverse(false);
        autoExecutionTimeline.setCycleCount(Timeline.INDEFINITE);

        // Auto execution tick timeline.
        executionTickTimeline = new Timeline();
    }

    /**
     * Create a new model controller for {@link ExecutionModel#INSTANCE}.
     * 
     */
    public ExecutionModelController () {
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

                List<Operation> executedOperations = executionModel.executeNext();

                if (operationsExecutedListener != null) {
                    operationsExecutedListener.operationsExecuted(executedOperations);
                }
            } else {
                stopAutoExecution();
            }
        });

        if (executionTickListener != null) {
            startExecutionTickUpdates();
        }

        autoExecutionTimeline.getKeyFrames().add(executionFrame);
        autoExecutionTimeline.play();
    }

    private void startExecutionTickUpdates () {

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
     * Control - one for one copies
     *
     */
    // ============================================================= //

}
