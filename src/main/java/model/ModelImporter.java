package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import contract.Locator;
import contract.Operation;
import contract.datastructure.DataStructure;
import contract.datastructure.IndependentElement;
import contract.operation.Key;
import contract.operation.OperationUtility;
import gui.Main;
import gui.dialog.CreateStructureDialog;
import gui.dialog.IdentifierCollisionDialog;

/**
 * 
 * @author Richard
 *
 */
public class ModelImporter {

    // ============================================================= //
    /*
     *
     * Field variables
     *
     */
    // ============================================================= //

    /**
     * The live model.
     */
    private final Model       liveModel;

    /**
     * Automatic removal setting.
     */
    private boolean           autoRemoveUnsued = true;

    /**
     * Automatic add setting.
     */
    private boolean           autoCreateOrphan = false;

    /**
     * Indicates that old data should always be cleared in case of an identifier
     * collision.
     */
    private boolean           alwaysClearOld   = false;
    /**
     * Indicates that new data should always be rejected in case of an identifier
     * collision.
     */
    private boolean           alwaysKeepOld    = false;

    /**
     * List of used identifiers.
     */
    private ArrayList<String> usedNames;

    /**
     * List of unused identifiers.
     */
    private ArrayList<String> unusedNames;

    /**
     * List of rejected identifiers.
     */
    private ArrayList<String> removedNames;

    /**
     * List of the <b>data structure</b> (not operation type) names present in a list of
     * operations.
     */
    private Set<String>       operationStructNames;

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
    public ModelImporter (Model liveModel) {
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
     * @return {@code false} if the live model hasn't changed. True if there is a
     *         possibility that is has.
     */
    public boolean insertIntoLiveModel (List<Operation> newOps, Map<String, DataStructure> newStructs) {

        /*
         * Handle structure name collision.
         */
        boolean nameCollision = checkNameCollision(newStructs.keySet(), liveModel.getStructures().keySet());

        if (nameCollision) {
            IdentifierCollisionDialog icd = new IdentifierCollisionDialog(null);

            short icdReturnedRoutine = icd.show(liveModel.getStructures().values(), newStructs.values());
            boolean abortImport = handleNameCollisionRespone(icdReturnedRoutine);

            if (abortImport) {
                return false;
            }
        }

        /*
         * Handle used undeclared structures and unused structures.
         */

        // Collect usage data.
        runUseageTest(newOps, newStructs);
        gatherUsageData(newOps, newStructs);

        // Handle unused names.
        handleUnusedNames(newStructs);

        // Handle used but undeclared names.
        gatherUsedOperationNames(newOps, newStructs);
        handleUndeclaredNames(newStructs.keySet());

        Map<String, DataStructure> newLiveModelStructures = new HashMap<String, DataStructure>();
        newLiveModelStructures.putAll(liveModel.getStructures());
        newLiveModelStructures.putAll(newStructs);

        List<Operation> newLiveModelOperations = new ArrayList<Operation>();

        liveModel.set(newLiveModelStructures, newLiveModelOperations);
        return true;
    }

    // ============================================================= //
    /*
     *
     * Utility
     *
     */
    // ============================================================= //

    private void handleUndeclaredNames (Set<String> newStructNames) {
        Set<String> allStructNames = new HashSet<String>();
        allStructNames.addAll(newStructNames);
        allStructNames.addAll(liveModel.getStructures().keySet());

        DataStructure newStruct;
        System.out.println("used = " + operationStructNames);
        System.out.println("known = " + allStructNames);
        for (String identifier : operationStructNames) {

            if (!allStructNames.contains(identifier)) {
                if (autoCreateOrphan) {
                    autoCreateOrphan(identifier);
                } else {
                    CreateStructureDialog ccd = new CreateStructureDialog(null);
                    newStruct = ccd.show(identifier);

                    if (newStruct != null) {
                        liveModel.getStructures().put(newStruct.identifier, newStruct);
                    }
                }
            }
            
        }
    }

    private void autoCreateOrphan (String identifier) {
        IndependentElement newStruct = new IndependentElement(identifier, null, null, null);
        liveModel.getStructures().put(newStruct.identifier, newStruct);
    }

    private void gatherUsedOperationNames (List<Operation> ops, Map<String, DataStructure> structs) {
        operationStructNames = new HashSet<String>();

        // Gather all operation identifiers.
        for (Operation op : ops) {
            switch (op.operation) {
            case message:
                break;
            case read:
            case write:

                Locator source = OperationUtility.getLoactor(op, Key.source);
                if (source != null) {
                    DataStructure sourceStruct = structs.get(source.identifier);
                    if (sourceStruct == null) {
                        operationStructNames.add(source.identifier);
                    }
                }

                Locator target = OperationUtility.getLoactor(op, Key.target);
                if (target != null) {
                    DataStructure targetStruct = structs.get(target.identifier);
                    if (targetStruct == null) {
                        operationStructNames.add(target.identifier);
                    }
                }
                break;
            case swap:
                Locator var1 = OperationUtility.getLoactor(op, Key.var1);

                if (var1 != null) {
                    DataStructure var1Struct = structs.get(var1.identifier);
                    if (var1Struct == null) {
                        operationStructNames.add(var1.identifier);
                    }
                }

                Locator var2 = OperationUtility.getLoactor(op, Key.target);
                if (var2 != null) {
                    DataStructure var2Struct = structs.get(var2.identifier);
                    if (var2Struct == null) {
                        operationStructNames.add(var2.identifier);
                    }
                }
                break;
            case remove:
                String identifier = OperationUtility.getIdentifier(op);
                DataStructure targetStruct = structs.get(identifier);
                if (targetStruct == null) {
                    operationStructNames.add(identifier);
                }
                break;
            }
        }
    }

    private boolean handleNameCollisionRespone (short foo) {
        boolean abortImport = false;

        if (alwaysClearOld) {
            liveModel.hardClear();
            return false;
        } else if (alwaysKeepOld) {
            return true;
        }

        switch (foo) {

        case IdentifierCollisionDialog.CLEAR_OLD_ALWAYS:
            alwaysClearOld = true;
        case IdentifierCollisionDialog.CLEAR_OLD:
            liveModel.hardClear();
            abortImport = false;
            break;

        case IdentifierCollisionDialog.KEEP_OLD_ALWAYS:
            alwaysKeepOld = true;
        case IdentifierCollisionDialog.KEEP_OLD:
            abortImport = true;
        }

        return abortImport;
    }

    private void handleUnusedNames (Map<String, DataStructure> newStructs) {
        if (autoRemoveUnsued) {
            for (String name : unusedNames) {
                newStructs.remove(name);
                removedNames.add(name);
            }
        } else {
            // TODO: Implement removal of unused names prompt.
        }

        if (!removedNames.isEmpty()) {
            Main.console.force("Ignored unused stuctures: " + removedNames);
        }
    }

    private void gatherUsageData (List<Operation> newOps, Map<String, DataStructure> newStructs) {
        usedNames = new ArrayList<String>();
        unusedNames = new ArrayList<String>();
        removedNames = new ArrayList<String>();

        newOps.addAll(liveModel.getOperations());
        newStructs.putAll(liveModel.getStructures());

        for (DataStructure struct : newStructs.values()) {
            if (struct.isApplyOperationCalled()) {
                usedNames.add(struct.identifier);
            } else {
                unusedNames.add(struct.identifier);
            }
        }
    }

    /**
     * Test usage of structures without altering the {@code liveModel}.
     * 
     * @param newOps
     *            The list of new operations.
     * @param newStructs
     *            The map of new structures.
     */
    public void runUseageTest (List<Operation> newOps, Map<String, DataStructure> newStructs) {
        Model testModel = new Model();
        // Add operations and structures from the live model.
        newStructs.putAll(liveModel.getStructures());
        newOps.addAll(liveModel.getOperations());

        // Set and run
        testModel.set(newStructs, newOps);
        testModel.goToEnd();
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
    public static boolean checkNameCollision (Collection<String> newNames, Collection<String> oldNames) {
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
