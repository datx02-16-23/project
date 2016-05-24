package contract.utility;

import contract.Locator;
import contract.operation.Key;

public class Tools2 {

    /**
     * 
     * @param key
     * @return
     */
    public static Locator getLocator (Key key) {
        Locator locator = null;

        switch (key) {
        case source:
            break;
        case target:
            break;
        case var1:
            break;
        case var2:
            break;
        default:
            System.err.println("Key " + key + " does not return a Locator.");
            break;
        }

        return locator;
    }
}
