package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.OspfNeighbor;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class OspfNeighborMatchersImpl {

  static final class HasRemoteOspfNeighbor extends FeatureMatcher<OspfNeighbor, OspfNeighbor> {
    HasRemoteOspfNeighbor(@Nonnull Matcher<? super OspfNeighbor> subMatcher) {
      super(subMatcher, "OspfNeighbor with remoteOspfNeighbor", "remoteOspfNeighbor");
    }

    @Override
    protected OspfNeighbor featureValueOf(OspfNeighbor actual) {
      return actual.getRemoteOspfNeighbor();
    }
  }
}
