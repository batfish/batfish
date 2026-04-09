package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** Matchers for {@link org.batfish.datamodel.ospf.OspfAreaSummary}. */
public final class OspfAreaSummaryMatchers {

  /** Returns a matcher asserting that the OSPF summary route has the specified metric. */
  public static Matcher<OspfAreaSummary> hasMetric(long metric) {
    return new HasMetric(equalTo(metric));
  }

  /** Returns a matcher asserting that the OSPF summary route has a matching metric. */
  public static Matcher<OspfAreaSummary> hasMetric(Matcher<? super Long> metricMatcher) {
    return new HasMetric(metricMatcher);
  }

  /** Returns a matcher asserting that the OSPF summary route will be advertised. */
  public static Matcher<OspfAreaSummary> isAdvertised() {
    return new IsAdvertised(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF area
   * summary's advertised flag.
   */
  public static Matcher<OspfAreaSummary> isAdvertised(Matcher<? super Boolean> subMatcher) {
    return new IsAdvertised(subMatcher);
  }

  /** Returns a matcher asserting that the OSPF summary route will be advertised. */
  public static Matcher<OspfAreaSummary> installsDiscard() {
    return new InstallsDiscard(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF area
   * summary's advertised flag.
   */
  public static Matcher<OspfAreaSummary> installsDiscard(Matcher<? super Boolean> subMatcher) {
    return new InstallsDiscard(subMatcher);
  }

  private OspfAreaSummaryMatchers() {}

  private static final class HasMetric extends FeatureMatcher<OspfAreaSummary, Long> {
    HasMetric(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An OspfAreaSummary with metric:", "metric");
    }

    @Override
    protected Long featureValueOf(OspfAreaSummary actual) {
      return actual.getMetric();
    }
  }

  private static final class IsAdvertised extends FeatureMatcher<OspfAreaSummary, Boolean> {
    IsAdvertised(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An OspfAreaSummary with advertised:", "advertised");
    }

    @Override
    protected Boolean featureValueOf(OspfAreaSummary arg0) {
      return arg0.isAdvertised();
    }
  }

  private static final class InstallsDiscard extends FeatureMatcher<OspfAreaSummary, Boolean> {
    InstallsDiscard(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An OspfAreaSummary that installs discard:", "installs discard");
    }

    @Override
    protected Boolean featureValueOf(OspfAreaSummary arg0) {
      return arg0.installsDiscard();
    }
  }
}
