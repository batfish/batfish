package org.batfish.datamodel.matchers;

import java.util.Map;
import org.batfish.common.Pair;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfNeighbor;
import org.batfish.datamodel.matchers.OspfProcessMatchersImpl.HasAreas;
import org.batfish.datamodel.matchers.OspfProcessMatchersImpl.HasOspfNeighbors;
import org.hamcrest.Matcher;

public class OspfProcessMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF process's
   * areas.
   */
  public static HasAreas hasAreas(Matcher<? super Map<Long, OspfArea>> subMatcher) {
    return new HasAreas(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF process's
   * OSPF neighbors.
   */
  public static HasOspfNeighbors hasOspfNeighbors(
      Matcher<? super Map<Pair<Ip, Ip>, OspfNeighbor>> subMatcher) {
    return new HasOspfNeighbors(subMatcher);
  }
}
