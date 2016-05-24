package assets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import contract.Operation;
import contract.datastructure.DataStructure;
import gui.Main;
import model.Model;

/**
 * 
 * @author Richard
 *
 */
public class ModelManager {

    // ============================================================= //
    /*
     *
     * Field variables
     *
     */
    // ============================================================= //

    /**
     * The live model
     */
    private final Model liveModel;
    /**
     * The model to run tests on.
     */
    private Model       testModel;

    /**
     * Automatic removal setting.
     */
    private boolean     autoRemoveUnsued = true;

    /**
     * Automatic add setting.
     */
    private boolean     autoCreateOrphan = false;

    // ============================================================= //
    /*
     *
     * Constructors
     *
     */
    // ============================================================= //

    /**
     * Creates a new ModelManager for the live model
     * 
     * @param liveModel
     *            The live model in use.
     */
    public ModelManager (Model liveModel) {
        this.liveModel = liveModel;
    }

    // ============================================================= //
    /*
     *
     * Control
     *
     */
    // ============================================================= //

    /**
     * Attempt to insert structures and operations into a live model.
     * 
     * @param ops
     * @param structures
     * @return
     */
    public void importToModel (List<Operation> ops, Map<String, DataStructure> structures) {

        /*
         * Handle structure name collision.
         */
        boolean identifierCollision = checkIdentifierCollision(structures.keySet(), liveModel.getStructures().keySet());

        if (identifierCollision) {

        }

        /*
         * Handle used/unused operations
         */
        testModel = new Model();
        // Add operations and structures from the live model.
        structures.putAll(liveModel.getStructures());
        ops.addAll(liveModel.getOperations());
        
        // Set and run
        testModel.set(structures, ops);
        testModel.goToEnd();

        /*
         * Collect usage data. 
         */
        
        ArrayList<String> used = new ArrayList<String>();
        ArrayList<String> notUsed = new ArrayList<String>();

        ArrayList<String> removedNames = new ArrayList<String>();
        ArrayList<String> addedNames = new ArrayList<String>();

        ops.addAll(liveModel.getOperations());
        structures.putAll(liveModel.getStructures());

        for (DataStructure struct : structures.values()) {

            if (struct.isApplyOperationCalled()) {
                used.add(struct.identifier);
            } else {
                notUsed.add(struct.identifier);
            }
        }

        /*
         * Removed unused structures.
         */
        if (autoRemoveUnsued) {
            for (String name : notUsed) {
                structures.remove(name);
                removedNames.add(name);
            }
        } else {
            // TODO prompt user.
        }

        if (!removedNames.isEmpty()) {
            Main.console.force("Unused stuctures ignored: " + removedNames);
        }

        /*
         * Added implied structures.
         */
        // TODO implement
        if (autoCreateOrphan) {

        } else {

        }
    }

    /**
     * Check for collision between keys
     * 
     * @param newNames
     *            The new identifiers.
     * @param oldNames
     *            The old identifiers.
     * @return {@code} if there was a collision, false otherwise.
     */
    public static boolean checkIdentifierCollision (Collection<String> newNames, Collection<String> oldNames) {
        for (String aNewName : newNames) {
            if (oldNames.contains(aNewName)) {
                return true;
            }
        }
        return false;
    }

    // ============================================================= //
    /*
     *
     * Getters and Setters
     *
     */
    // ============================================================= //

    /**
     * If {@code true}, unused structures will be removed automatically.
     * 
     * @param autoRemoveUnsued
     *            The automatic removal settings.
     */
    public void setAutoRemoveUnsued (boolean autoRemoveUnsued) {
        this.autoRemoveUnsued = autoRemoveUnsued;
    }

    /**
     * If {@code true}, undeclared structures will be created as Orphans
     * automatically.
     * 
     * @param autoRemoveUnsued
     *            The automatic adding of orphans settings.
     */
    public void setAutoCreateOrphan (boolean autoCreateOrphan) {
        this.autoCreateOrphan = autoCreateOrphan;
    }
}
