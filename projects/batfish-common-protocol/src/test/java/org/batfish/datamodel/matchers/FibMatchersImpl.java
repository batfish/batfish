package org.batfish.datamodel.matchers;

import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.Ip;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class FibMatchersImpl {

  static class HasNextHopInterfaces
      extends FeatureMatcher<Fib, Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>> {

    HasNextHopInterfaces(
        Matcher<? super Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>> subMatcher) {
      super(subMatcher, "A FIB with nextHopInterfaces:", "nextHopInterfaces");
    }

    @Override
    protected Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> featureValueOf(
        Fib actual) {
      return actual.getNextHopInterfaces();
    }
  }

  private FibMatchersImpl() {}
}
