package gui.panel;

import assets.Const;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 * Printout of error messages and warnings from the program. Strings only. Use Object toString
 * to print them.
 *
 * @author Richard Sundqvist
 *
 */
public class ConsolPanel {

    public static final String PREPEND_FORCE = "\n<>\t";
    public static final String PREPEND_ERR   = "\n>\t";
    public static final String PREPEND_INFO  = "\n";
    public static final String PREPEND_DEBUG = "\n";
    
    // ============================================================= //
    /*
     *
     * Field variables
     *
     */
    // ============================================================= //
    
    private boolean              quiet         = false;
    private boolean              info          = true;
    private boolean              err           = true;
    private boolean              debug         = false;
    
    private final TextArea      consoleTextArea;

    // ============================================================= //
    /*
     *
     * Constructors
     *
     */
    // ============================================================= //
    
    public ConsolPanel (TextArea consoleTextArea) {
        this.consoleTextArea = consoleTextArea;
        consoleTextArea.setEditable(false);
        init();
    }
    
    // ============================================================= //
    /*
     *
     * Control
     *
     */
    // ============================================================= //

    /**
     * Clear the console.
     */
    public void clear () {
        init();
    }
    
    /**
     * Print a regular line to the GUI console.
     *
     * @param info
     *            The line to prine.
     */
    public void info (String info) {
        if (quiet || !this.info) {
            return;
        }
        print(PREPEND_INFO + info);
    }

    /**
     * Print an error to the GUI console.
     *
     * @param err
     *            The error to print.
     */
    public void err (String err) {
        if (quiet || !this.err) {
            return;
        }
        print(PREPEND_ERR + err);
    }

    /**
     * Print a debug String. Generally DISABLED.
     *
     * @param debug
     *            A debug String to print.
     */
    public void debug (String debug) {
        if (quiet || !this.debug) {
            print(PREPEND_DEBUG + debug);
        }
    }

    /**
     * Print a line regardless of settings.
     *
     * @param force
     *            The line to print.
     */
    public void force (String force) {
        print((force.equals("") ? "" : PREPEND_FORCE) + force);
    }

    // ============================================================= //
    /*
     *
     * Utility
     *
     */
    // ============================================================= //
    
    /**
     * Print the given String. Runs on JavaFX Application thread.
     *
     * @param string
     *            The string to print to the console.
     */
    private void print (String string) {
        Platform.runLater( () -> ConsolPanel.this.consoleTextArea.appendText(string));
    }
    
    private void init () {
        StringBuilder sb = new StringBuilder();
        sb.append(Const.PROGRAM_NAME + " version " + Const.VERSION_NUMBER);
        sb.append("\nAUTHORS: ");
        for (String s : Const.DEVELOPER_NAMES) {
            sb.append(s + " | ");
        }
        sb.replace(sb.length() - 2, sb.length(), "\n");
        String initMessage = sb.toString();
        Platform.runLater( () -> ConsolPanel.this.consoleTextArea.setText(initMessage));
    }
    
    // ============================================================= //
    /*
     *
     * Setters and Getters
     *
     */
    // ============================================================= //

    /**
     * Enable or disable information printouts.
     *
     * @param value
     *            The setting to apply.
     */
    public void setInfo (boolean value) {
        info = value;
        if (!quiet) {
            print(PREPEND_INFO + "Information printouts " + (info ? "ENABLED." : "DISABLED."));
        }
    }

    /**
     * Enable or disable Quiet Mode.
     *
     * @param value
     *            The setting to apply.
     */
    public void setQuiet (boolean value) {
        quiet = value;
        force("Quiet Mode " + (quiet ? "ENABLED." : "DISABLED."));
    }

    /**
     * Enable or disable debug printouts.
     *
     * @param value
     *            The setting to apply.
     */
    public void setDebug (boolean value) {
        debug = value;
        if (!quiet) {
            print(PREPEND_DEBUG + "Debug printouts " + (debug ? "ENABLED." : "DISABLED."));
        }
    }

    /**
     * Enable or disable error printouts.
     *
     * @param value
     *            The setting to apply.
     */
    public void setError (boolean value) {
        err = value;
        if (!quiet) {
            print(PREPEND_ERR + "Error printouts " + (err ? "ENABLED." : "DISABLED."));
        }
    }
}