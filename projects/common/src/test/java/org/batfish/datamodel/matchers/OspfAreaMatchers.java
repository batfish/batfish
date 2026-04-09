package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ospf.NssaSettings;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.StubSettings;
import org.batfish.datamodel.ospf.StubType;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class OspfAreaMatchers {

  /** Provides a matcher that matches if the OSPF area's injectDefaultRoute is true. */
  public static Matcher<OspfArea> hasInjectDefaultRoute() {
    return new HasInjectDefaultRoute();
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF area's
   * interfaces.
   */
  public static Matcher<OspfArea> hasInterfaces(Matcher<? super Set<String>> subMatcher) {
    return new HasInterfaces(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provide {@code subMatcher} matches the OSPF area's
   * metricOfDefaultRoute.
   */
  public static Matcher<OspfArea> hasMetricOfDefaultRoute(Matcher<? super Integer> subMatcher) {
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
  public static Matcher<OspfArea> hasSummary(
      Prefix summaryPrefix, Matcher<? super OspfAreaSummary> subMatcher) {
    return new HasSummary(summaryPrefix, subMatcher);
  }

  private OspfAreaMatchers() {}

  private static final class HasInjectDefaultRoute extends FeatureMatcher<OspfArea, Boolean> {
    HasInjectDefaultRoute() {
      super(is(true), "an OspfArea with injectDefaultRoute", "injectDefaultRoute");
    }

    @Override
    protected Boolean featureValueOf(OspfArea actual) {
      return actual.getInjectDefaultRoute();
    }
  }

  private static final class HasInterfaces extends FeatureMatcher<OspfArea, Set<String>> {
    HasInterfaces(@Nonnull Matcher<? super Set<String>> subMatcher) {
      super(subMatcher, "an OspfArea with interfaces:", "interfaces");
    }

    @Override
    protected Set<String> featureValueOf(OspfArea actual) {
      return actual.getInterfaces();
    }
  }

  private static final class HasMetricOfDefaultRoute extends FeatureMatcher<OspfArea, Integer> {
    HasMetricOfDefaultRoute(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an OspfArea with metricOfDefaultRoute:", "metricOfDefaultRoute");
    }

    @Override
    protected Integer featureValueOf(OspfArea actual) {
      return actual.getMetricOfDefaultRoute();
    }
  }

  private static final class HasNssa extends FeatureMatcher<OspfArea, NssaSettings> {
    public HasNssa(@Nonnull Matcher<? super NssaSettings> subMatcher) {
      super(subMatcher, "An OspfArea with nssa:", "nssa");
    }

    @Override
    protected NssaSettings featureValueOf(OspfArea actual) {
      return actual.getNssa();
    }
  }

  private static final class HasStub extends FeatureMatcher<OspfArea, StubSettings> {
    public HasStub(@Nonnull Matcher<? super StubSettings> subMatcher) {
      super(subMatcher, "An OspfArea with stub:", "stub");
    }

    @Override
    protected StubSettings featureValueOf(OspfArea actual) {
      return actual.getStub();
    }
  }

  private static final class HasStubType extends FeatureMatcher<OspfArea, StubType> {

    public HasStubType(@Nonnull Matcher<? super StubType> subMatcher) {
      super(subMatcher, "An OspfArea with stubType:", "stubType");
    }

    @Override
    protected StubType featureValueOf(OspfArea actual) {
      return actual.getStubType();
    }
  }

  private static final class HasSummary extends FeatureMatcher<OspfArea, OspfAreaSummary> {
    private final Prefix _summaryPrefix;

    HasSummary(
        @Nonnull Prefix summaryPrefix, @Nonnull Matcher<? super OspfAreaSummary> subMatcher) {
      super(
          subMatcher,
          "an OspfArea with summary " + summaryPrefix + ":",
          "summary " + summaryPrefix);
      _summaryPrefix = summaryPrefix;
    }

    @Override
    protected OspfAreaSummary featureValueOf(OspfArea actual) {
      return actual.getSummaries().get(_summaryPrefix);
    }
  }
}
