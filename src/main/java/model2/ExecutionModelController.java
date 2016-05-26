package model2;

import java.util.List;

import assets.Debug;
import contract.json.Operation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.util.Duration;
import render.assets.Const;
import render.assets.Visualization2;

/**
 * ExecutionModel convenience class.
 * 
 * @author Richard Sundqvist
 *
 */
public class ExecutionModelController implements OperationsExecutedListener {

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
    private final ExecutionModel  executionModel;

    /**
     * The visualization used to animate the model.
     */
    private final Visualization2  visualization;

    /**
     * Time line used for timed model progression.
     */
    private final Timeline        autoExecutionTimeline;

    /**
     * Time line used for timed model progression.
     */
    private final Timeline        executionTickTimeline;

    /**
     * The number of ticks per execution call. That is, the number of times the the
     * {@code executionTickListener} will be called for each time the
     * {@code operationsExecutedListener} will be called.
     */
    private int                   executionTickCount;

    /**
     * The current tick number.
     */
    private int                   currentExecutionTick;

    /**
     * The tick listener for the controller.
     */
    private ExecutionTickListener executionTickListener;

    /**
     * The delay between ticks.
     */
    private long                  autoExecutionSpeed;

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
     * @param The
     *            The visualization used to animate.
     */
    public ExecutionModelController (ExecutionModel executionModel, Visualization2 visualization) {
        this.executionModel = executionModel;
        this.visualization = visualization;

        executionModel.setOperationsExecutedListener(this);

        autoExecutionSpeed = Const.DEFAULT_ANIMATION_TIME;

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
        this(ExecutionModel.INSTANCE, new Visualization2(ExecutionModel.INSTANCE));
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
     */
    public void startAutoExecution () {
        autoExecutingProperty.set(true);
        startAutoExecution(autoExecutionSpeed);
    }

    /**
     * Begin timed execution for the model.
     * 
     * @param millis
     *            The time between executions.
     */
    public void startAutoExecution (long millis) {

        KeyFrame executionFrame = new KeyFrame(Duration.millis(millis), event -> {

            currentExecutionTick = 1; // Reset the tick counter.

            if (executionModel.tryExecuteNext()) {
                executeNext();
                startExecutionTickUpdates(millis);
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println("execute next!");
                System.out.println("execute next!");
                System.out.println("execute next!");
                System.out.println("execute next!");
                System.out.println("execute next!");
                System.out.println("execute next!");
            } else {
                stopAutoExecution();
                executionTickListener.update(0);
            }
        });

        autoExecutionTimeline.getKeyFrames().clear();
        autoExecutionTimeline.getKeyFrames().add(executionFrame);
        autoExecutionTimeline.play();
    }

    /**
     * Start execution tick updates, if there is a listener.
     * 
     * @param millis
     *            The time between executions.
     */
    private void startExecutionTickUpdates (long millis) {
        if (executionTickListener != null) {
            executionTickTimeline.stop();
            currentExecutionTick = 1;

            KeyFrame executionFrame = new KeyFrame(Duration.millis(millis / executionTickCount), event -> {
                executionTickListener.update(currentExecutionTick++);
            });

            executionTickTimeline.setCycleCount(executionTickCount);
            executionTickTimeline.getKeyFrames().clear();
            executionTickTimeline.getKeyFrames().add(executionFrame);
            executionTickTimeline.playFromStart();
        }
    }

    @Override public void operationsExecuted (List<Operation> executedOperations) {
        System.out.println("executedOperations = " + executedOperations);
        for (Operation op : executedOperations) {
            visualization.render(op);
        }
        executedOperations.clear();
    }

    /**
     * Stop timed execution for the model.
     */
    public void stopAutoExecution () {
        autoExecutingProperty.set(false);
        autoExecutionTimeline.stop();
        executionTickTimeline.stop();
    }

    // ============================================================= //
    /*
     *
     * Setters and Getters
     *
     */
    // ============================================================= //

    /**
     * Set the auto execution speed in milliseconds. Will stop auto execution, update the
     * speed, and resume if auto execution was on when this method was called.
     * 
     * @param autoExecutionSpeed
     *            The time between execution calls in milliseconds.
     * @throws IllegalArgumentException
     *             If {@code autoExecutionSpeed < 0}.
     */
    public void setAutoExecutionSpeed (long autoExecutionSpeed) {
        if (autoExecutionSpeed < 0) {
            throw new IllegalArgumentException("Time between executions cannot be less than zero.");
        }

        visualization.setAnimationTime(autoExecutionSpeed);

        boolean wasRunning = autoExecutionTimeline.getStatus() == Status.RUNNING;
        if (wasRunning) {
            stopAutoExecution();
        }

        this.autoExecutionSpeed = autoExecutionSpeed;
        autoExecutionSpeedProperty.set(autoExecutionSpeed);

        if (wasRunning) {
            startAutoExecution();
        }
    }

    /**
     * Returns the time between execution calls in milliseconds.
     * 
     * @return The time between execution calls in milliseconds.
     */
    public long getAutoExecutionSpeed () {
        return autoExecutionSpeed;
    }

    /**
     * Set listener and number of ticks per execution call. That is, the number of times
     * the the {@code executionTickListener} will be called for each time the
     * {@code operationsExecutedListener} will be called.
     *
     * @param executionTickListener
     *            An {@code ExecutionTickListener}.
     * @param tickCount
     *            The number of ticks per execution cycle.
     * @throws IllegalArgumentException
     *             If {@code tickCount < 0}.
     */
    public void setExecutionTickListener (ExecutionTickListener executionTickListener, int tickCount) {
        if (tickCount < 1) {
            throw new IllegalArgumentException("tickCount cannot be less than zero.");
        }

        if (Debug.ERR) {
            System.err.println("ExecutionTickListener set to " + executionTickListener);
        }

        this.executionTickListener = executionTickListener;
        this.executionTickCount = tickCount;
    }

    /**
     * Returns the execution model for this controller.
     * 
     * @return An {@code ExecutionModel}.
     */
    public ExecutionModel getExecutionModel () {
        return executionModel;
    }

    // ============================================================= //
    /*
     *
     * Properties / Getters and Setters
     *
     */
    // ============================================================= //

    private final ReadOnlyLongWrapper    autoExecutionSpeedProperty = new ReadOnlyLongWrapper(autoExecutionSpeed);
    private final ReadOnlyBooleanWrapper autoExecutingProperty      = new ReadOnlyBooleanWrapper(false);

    /**
     * Returns a property indicating whether auto execution is currently on.
     * 
     * @return A ReadOnlyLongProperty.
     */
    public ReadOnlyBooleanProperty autoExecutingProperty () {
        return autoExecutingProperty.getReadOnlyProperty();
    }

    /**
     * Returns a property indicating the time between execution calls when using autoplay,
     * in milliseconds.
     * 
     * @return A ReadOnlyLongProperty.
     */
    public ReadOnlyLongProperty autoExecutionSpeedProperty () {
        return autoExecutionSpeedProperty.getReadOnlyProperty();
    }

    // ============================================================= //
    /*
     *
     * Control - ExecutionModel wrappers
     *
     */
    // ============================================================= //

    /**
     * @see model2.ExecutionModel#executeNext()
     */
    public void executeNext () {
        executionModel.executeNext();
        startExecutionTickUpdates(autoExecutionSpeed);
    }

    /**
     * @see model2.ExecutionModel#executePrevious()
     */
    public void executePrevious () {
        executionModel.executePrevious();
        startExecutionTickUpdates(autoExecutionSpeed);
    }

    /**
     * @param toIndex
     * @return
     * @see model2.ExecutionModel#execute(int)
     */
    public void execute (int toIndex) {
        executionModel.execute(toIndex);
        startExecutionTickUpdates(autoExecutionSpeed);
    }

    /**
     * 
     * @see model2.ExecutionModel#reset()
     */
    public void reset () {
        visualization.reset();
        executionModel.reset();
    }

    /**
     * 
     * @see model2.ExecutionModel#clear()
     */
    public void clear () {
        visualization.clear();
        executionModel.clear();
    }
}
