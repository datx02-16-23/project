package assets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import contract.Operation;
import contract.datastructure.DataStructure;
import gui.Main;
import gui.dialog.CreateStructureDialog;
import gui.dialog.IdentifierCollisionDialog;
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
    private final Model                     liveModel;
    /**
     * The model to run tests on.
     */
    private Model                           testModel;

    /**
     * Automatic removal setting.
     */
    private boolean                         autoRemoveUnsued = true;

    /**
     * Automatic add setting.
     */
    private boolean                         autoCreateOrphan = false;

    /**
     * User decision dialog in case of structure name collision.
     */
    private final IdentifierCollisionDialog icd              = new IdentifierCollisionDialog(null);
    /**
     * Indicates that old data should always be cleared in case of an identifier collision.
     */
    private boolean                         alwaysClearOld   = false;
    /**
     * Indicates that new data should always be rejected in case of an identifier collision.
     */
    private boolean                         alwaysKeepOld    = false;

    /**
     * User decision dialog in case a missing structure is found.
     */
    private final CreateStructureDialog     csd              = new CreateStructureDialog(null);

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
     * @param newOps
     *            The new operations to insert.
     * @param newStructs
     *            The new data structures to insert.
     * @return {@code true} if the live model changed, false otherwise.
     */
    public boolean importToModel (List<Operation> newOps, Map<String, DataStructure> newStructs) {
        boolean changed = false;

        /*
         * Handle structure name collision.
         */
        boolean identifierCollision = checkIdentifierCollision(newStructs.keySet(), liveModel.getStructures().keySet());

        if (identifierCollision) {
            short foo = icd.show(liveModel.getStructures().values(), newStructs.values());
            switch (foo) {
            case IdentifierCollisionDialog.ALWAYS_CLEAR_OLD:
                break;
            case IdentifierCollisionDialog.ALWAYS_KEEP_OLD:
                break;
            case IdentifierCollisionDialog.CLEAR_OLD:
                break;
            case IdentifierCollisionDialog.KEEP_OLD:
                break;
            }
        }

        /*
         * Handle used/unused operations
         */
        testModel = new Model();
        // Add operations and structures from the live model.
        newStructs.putAll(liveModel.getStructures());
        newOps.addAll(liveModel.getOperations());

        // Set and run
        testModel.set(newStructs, newOps);
        testModel.goToEnd();

        /*
         * Collect usage data.
         */

        ArrayList<String> used = new ArrayList<String>();
        ArrayList<String> notUsed = new ArrayList<String>();

        ArrayList<String> removedNames = new ArrayList<String>();
        ArrayList<String> addedNames = new ArrayList<String>();

        newOps.addAll(liveModel.getOperations());
        newStructs.putAll(liveModel.getStructures());

        for (DataStructure struct : newStructs.values()) {
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
                newStructs.remove(name);
                removedNames.add(name);
            }
        } else {
            // TODO prompt user.
        }

        if (!removedNames.isEmpty()) {
            Main.console.force("Ignored unused stuctures: " + removedNames);
        }

        /*
         * Added implied structures.
         */
        // TODO implement
        if (autoCreateOrphan) {

        } else {

        }

        return changed;
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
     * If {@code true}, undeclared structures will be created as Orphans automatically.
     * 
     * @param autoRemoveUnsued
     *            The automatic adding of orphans settings.
     */
    public void setAutoCreateOrphan (boolean autoCreateOrphan) {
        this.autoCreateOrphan = autoCreateOrphan;
    }
}
