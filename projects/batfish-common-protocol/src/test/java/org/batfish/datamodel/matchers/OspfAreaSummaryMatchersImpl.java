package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.OspfAreaSummary;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class OspfAreaSummaryMatchersImpl {

  static final class IsAdvertised extends BaseMatcher<OspfAreaSummary> {
    @Override
    public boolean matches(Object item) {
      return item instanceof OspfAreaSummary && ((OspfAreaSummary) item).getAdvertise();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("is advertised");
    }
  }

  static final class HasMetric extends FeatureMatcher<OspfAreaSummary, Long> {
    HasMetric(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "metric", "metric");
    }

    @Override
    protected Long featureValueOf(OspfAreaSummary actual) {
      return actual.getMetric();
    }
  }

  private OspfAreaSummaryMatchersImpl() {}
}
