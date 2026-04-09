package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.OspfNeighbor;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class OspfNeighborMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF neigbor's
   * remote OSPF neighbor.
   */
  public static Matcher<OspfNeighbor> hasRemoteOspfNeighbor(
      Matcher<? super OspfNeighbor> subMatcher) {
    return new HasRemoteOspfNeighbor(subMatcher);
  }

  private static final class HasRemoteOspfNeighbor
      extends FeatureMatcher<OspfNeighbor, OspfNeighbor> {
    HasRemoteOspfNeighbor(@Nonnull Matcher<? super OspfNeighbor> subMatcher) {
      super(subMatcher, "OspfNeighbor with remoteOspfNeighbor", "remoteOspfNeighbor");
    }

    @Override
    protected OspfNeighbor featureValueOf(OspfNeighbor actual) {
      return actual.getRemoteOspfNeighbor();
    }
  }
}
