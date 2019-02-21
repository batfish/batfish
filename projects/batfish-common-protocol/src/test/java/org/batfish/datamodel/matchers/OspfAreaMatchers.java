package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.matchers.OspfAreaMatchersImpl.HasInjectDefaultRoute;
import org.batfish.datamodel.matchers.OspfAreaMatchersImpl.HasInterfaces;
import org.batfish.datamodel.matchers.OspfAreaMatchersImpl.HasMetricOfDefaultRoute;
import org.batfish.datamodel.matchers.OspfAreaMatchersImpl.HasNssa;
import org.batfish.datamodel.matchers.OspfAreaMatchersImpl.HasStub;
import org.batfish.datamodel.matchers.OspfAreaMatchersImpl.HasStubType;
import org.batfish.datamodel.matchers.OspfAreaMatchersImpl.HasSummary;
import org.batfish.datamodel.ospf.NssaSettings;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.StubSettings;
import org.batfish.datamodel.ospf.StubType;
import org.hamcrest.Matcher;

public final class OspfAreaMatchers {

  /** Provides a matcher that matches if the OSPF area's injectDefaultRoute is true. */
  public static HasInjectDefaultRoute hasInjectDefaultRoute() {
    return new HasInjectDefaultRoute();
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF area's
   * interfaces.
   */
  public static HasInterfaces hasInterfaces(Matcher<? super Set<String>> subMatcher) {
    return new HasInterfaces(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provide {@code subMatcher} matches the OSPF area's
   * metricOfDefaultRoute.
   */
  public static HasMetricOfDefaultRoute hasMetricOfDefaultRoute(
      Matcher<? super Integer> subMatcher) {
    return new HasMetricOfDefaultRoute(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * OspfArea}'s nssa.
   */
  public static @Nonnull Matcher<OspfArea> hasNssa(
      @Nonnull Matcher<? super NssaSettings> subMatcher) {
    return new HasNssa(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * OspfArea}'s stub.
   */
  public static @Nonnull Matcher<OspfArea> hasStub(
      @Nonnull Matcher<? super StubSettings> subMatcher) {
    return new HasStub(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * OspfArea}'s stubType.
   */
  public static @Nonnull Matcher<OspfArea> hasStubType(
      @Nonnull Matcher<? super StubType> subMatcher) {
    return new HasStubType(subMatcher);
  }

  /**
   * Provides a matcher that matches if the the {@link OspfArea}'s stubType is {@code
   * expectedStubType}.
   */
  public static @Nonnull Matcher<OspfArea> hasStubType(@Nonnull StubType expectedStubType) {
    return new HasStubType(equalTo(expectedStubType));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF area's
   * summary with the specified summaryPrefix.
   */
  public static HasSummary hasSummary(
      Prefix summaryPrefix, Matcher<? super OspfAreaSummary> subMatcher) {
    return new HasSummary(summaryPrefix, subMatcher);
  }

  private OspfAreaMatchers() {}
}
