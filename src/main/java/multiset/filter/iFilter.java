package multiset.filter;

import java.util.Set;

import multiset.model.iValueContainer;

/**
 * Created by cb on 26/04/16.
 */
public interface iFilter {
    Set<iValueContainer> filter (iValueContainer a, iValueContainer b);
}
