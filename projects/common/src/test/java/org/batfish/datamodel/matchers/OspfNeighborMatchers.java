package org.batfish.datamodel.matchers;

import org.batfish.datamodel.matchers.OspfNeighborMatchersImpl.HasRemoteOspfNeighbor;
import org.batfish.datamodel.ospf.OspfNeighbor;
import org.hamcrest.Matcher;

public class OspfNeighborMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF neigbor's
   * remote OSPF neighbor.
   */
  public static HasRemoteOspfNeighbor hasRemoteOspfNeighbor(
      Matcher<? super OspfNeighbor> subMatcher) {
    return new HasRemoteOspfNeighbor(subMatcher);
  }
}
