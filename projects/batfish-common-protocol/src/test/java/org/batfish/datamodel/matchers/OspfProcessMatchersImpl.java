package org.batfish.datamodel.matchers;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfNeighbor;
import org.batfish.datamodel.ospf.OspfProcess;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class OspfProcessMatchersImpl {

  static final class HasArea extends FeatureMatcher<OspfProcess, OspfArea> {
    private final long _id;

    HasArea(long id, @Nonnull Matcher<? super OspfArea> subMatcher) {
      super(subMatcher, "An OSPF process with area " + id + ":", "area " + id);
      _id = id;
    }

    @Override
    protected OspfArea featureValueOf(OspfProcess actual) {
      return actual.getAreas().get(_id);
    }
  }

  static final class HasAreas extends FeatureMatcher<OspfProcess, Map<Long, OspfArea>> {
    HasAreas(@Nonnull Matcher<? super Map<Long, OspfArea>> subMatcher) {
      super(subMatcher, "An OSPF process with areas:", "areas");
    }

    @Override
    protected Map<Long, OspfArea> featureValueOf(OspfProcess actual) {
      return actual.getAreas();
    }
  }

  static final class HasOspfNeighbors
      extends FeatureMatcher<OspfProcess, Map<IpLink, OspfNeighbor>> {
    HasOspfNeighbors(@Nonnull Matcher<? super Map<IpLink, OspfNeighbor>> subMatcher) {
      super(subMatcher, "An OSPF process with ospfNeighbors:", "ospfNeighbors");
    }

    @Override
    protected Map<IpLink, OspfNeighbor> featureValueOf(OspfProcess actual) {
      return actual.getOspfNeighbors();
    }
  }

  static final class HasReferenceBandwidth extends FeatureMatcher<OspfProcess, Double> {
    HasReferenceBandwidth(@Nonnull Matcher<? super Double> subMatcher) {
      super(subMatcher, "An OspfProcess with referenceBandwidth:", "referenceBandwidth");
    }

    @Override
    protected Double featureValueOf(OspfProcess actual) {
      return actual.getReferenceBandwidth();
    }
  }

  static final class HasRouterId extends FeatureMatcher<OspfProcess, Ip> {
    HasRouterId(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A OSPF process with router id:", "router id");
    }

    @Override
    protected Ip featureValueOf(OspfProcess actual) {
      return actual.getRouterId();
    }
  }
}
