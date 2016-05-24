package contract;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import assets.Const;

/**
 * Header item for the {@code Wrapper} class.
 */
public class Header implements Serializable {

    /**
     * Version number for this class.
     */
    private static final long                   serialVersionUID = Const.VERSION_NUMBER;
    /**
     * Default value for the {@code version} field.
     */
    public static final int                     VERSION_UNKNOWN  = 0;
    /**
     * The version number for this file. Version 0 is reserved for when the version number is
     * unknown.
     */
    public final int                            version;
    /**
     * Declaration of annotated variables from the source.
     */
    public final Map<String, AnnotatedVariable> annotatedVariables;
    /**
     * A map of metadata identifiers and their contents.
     */
    public final Map<String, List<String>>      sources;

    /**
     * Create a Header item with the given version number and map of annotated variables. Version 0
     * is reserved for when the version number is unknown.
     *
     * @param version
     *            The version number for this file.
     * @param annotatedVariables
     *            Declaration of annotated variables from the source.
     * @param sources
     *            The sources for this Header.
     */
    public Header (int version, Map<String, AnnotatedVariable> annotatedVariables, Map<String, List<String>> sources) {
        this.version = version;
        this.annotatedVariables = annotatedVariables;
        this.sources = sources;
    }

    /**
     * Create a {@code Header} object with an unknown version number and no declared variables. You
     * may populate the HashMap {@code annotatedVariables} manually after creation.
     */
    public Header () {
        version = VERSION_UNKNOWN;
        annotatedVariables = new HashMap<String, AnnotatedVariable>();
        sources = new HashMap<String, List<String>>();
    }

    /**
     * Returns the sources contained in the metadata of this Header, if found. Null otherwise.
     *
     * @return A Map of source names as keys and their lines as a List.
     */
    @SuppressWarnings("unchecked") public final Map<String, List<String>> getSources () {
        if (sources == null) {
            return null;
        }
        Map<String, Object> objectMap = (Map<String, Object>) sources.get("sources");
        Map<String, List<String>> sources = new HashMap<String, List<String>>();
        for (String key : objectMap.keySet()) {
            List<String> lines = (List<String>) objectMap.get(key);
            sources.put(key, lines);
        }
        return sources;
    }

    @Override public String toString () {
        return "Header: v = " + version + ", annotatedVariables = " + annotatedVariables;
    }
}
