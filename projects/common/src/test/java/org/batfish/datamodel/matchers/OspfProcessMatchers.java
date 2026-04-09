package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfNeighbor;
import org.batfish.datamodel.ospf.OspfProcess;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class OspfProcessMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF process's
   * area with specified id.
   */
  public static Matcher<OspfProcess> hasArea(
      long id, @Nonnull Matcher<? super OspfArea> subMatcher) {
    return new HasArea(id, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF process's
   * areas.
   */
  public static Matcher<OspfProcess> hasAreas(
      @Nonnull Matcher<? super Map<Long, OspfArea>> subMatcher) {
    return new HasAreas(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF process's
   * OSPF neighbors.
   */
  public static Matcher<OspfProcess> hasOspfNeighbors(
      Matcher<? super Map<IpLink, OspfNeighbor>> subMatcher) {
    return new HasOspfNeighbors(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF process's
   * router ID.
   */
  public static Matcher<OspfProcess> hasRouterId(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasRouterId(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * OspfProcess}'s reference-bandwidth.
   */
  public static Matcher<OspfProcess> hasReferenceBandwidth(
      @Nonnull Matcher<? super Double> subMatcher) {
    return new HasReferenceBandwidth(subMatcher);
  }

  public static Matcher<OspfProcess> hasReferenceBandwidth(double expectedReferenceBandwidth) {
    return hasReferenceBandwidth(equalTo(expectedReferenceBandwidth));
  }

  private static final class HasArea extends FeatureMatcher<OspfProcess, OspfArea> {
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

  private static final class HasAreas extends FeatureMatcher<OspfProcess, Map<Long, OspfArea>> {
    HasAreas(@Nonnull Matcher<? super Map<Long, OspfArea>> subMatcher) {
      super(subMatcher, "An OSPF process with areas:", "areas");
    }

    @Override
    protected Map<Long, OspfArea> featureValueOf(OspfProcess actual) {
      return actual.getAreas();
    }
  }

  private static final class HasOspfNeighbors
      extends FeatureMatcher<OspfProcess, Map<IpLink, OspfNeighbor>> {
    HasOspfNeighbors(@Nonnull Matcher<? super Map<IpLink, OspfNeighbor>> subMatcher) {
      super(subMatcher, "An OSPF process with ospfNeighbors:", "ospfNeighbors");
    }

    @Override
    protected Map<IpLink, OspfNeighbor> featureValueOf(OspfProcess actual) {
      return actual.getOspfNeighbors();
    }
  }

  private static final class HasReferenceBandwidth extends FeatureMatcher<OspfProcess, Double> {
    HasReferenceBandwidth(@Nonnull Matcher<? super Double> subMatcher) {
      super(subMatcher, "An OspfProcess with referenceBandwidth:", "referenceBandwidth");
    }

    @Override
    protected Double featureValueOf(OspfProcess actual) {
      return actual.getReferenceBandwidth();
    }
  }

  private static final class HasRouterId extends FeatureMatcher<OspfProcess, Ip> {
    HasRouterId(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A OSPF process with router id:", "router id");
    }

    @Override
    protected Ip featureValueOf(OspfProcess actual) {
      return actual.getRouterId();
    }
  }
}
