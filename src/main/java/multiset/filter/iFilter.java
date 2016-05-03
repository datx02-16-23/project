package multiset.filter;

import multiset.model.iValueContainer;

import java.util.List;

/**
 * Created by cb on 26/04/16.
 */
public interface iFilter {
  Set<iValueContainer> evaluate(iValueContainer a, iValueContainer b);
}
