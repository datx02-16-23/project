package multiset.filter;

import multiset.model.iValueContainer;

import java.util.List;
import java.util.Set;

/**
 * Created by cb on 26/04/16.
 */
public interface iFilter {
	Set<iValueContainer> filter(iValueContainer a, iValueContainer b);
}
