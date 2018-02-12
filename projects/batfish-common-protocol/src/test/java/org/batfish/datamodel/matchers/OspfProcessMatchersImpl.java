package org.batfish.datamodel.matchers;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.Pair;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfNeighbor;
import org.batfish.datamodel.OspfProcess;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class OspfProcessMatchersImpl {

  static final class HasAreas extends FeatureMatcher<OspfProcess, Map<Long, OspfArea>> {
    HasAreas(@Nonnull Matcher<? super Map<Long, OspfArea>> subMatcher) {
      super(subMatcher, "areas", "areas");
    }

    @Override
    protected Map<Long, OspfArea> featureValueOf(OspfProcess actual) {
      return actual.getAreas();
    }
  }

  static final class HasOspfNeighbors
      extends FeatureMatcher<OspfProcess, Map<Pair<Ip, Ip>, OspfNeighbor>> {
    HasOspfNeighbors(@Nonnull Matcher<? super Map<Pair<Ip, Ip>, OspfNeighbor>> subMatcher) {
      super(subMatcher, "OspfProcess with ospfNeighbors", "ospfNeighbors");
    }

    @Override
    protected Map<Pair<Ip, Ip>, OspfNeighbor> featureValueOf(OspfProcess actual) {
      return actual.getOspfNeighbors();
    }
  }
}
