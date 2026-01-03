package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class OspfAreaSummaryMatchersImpl {

  static final class HasMetric extends FeatureMatcher<OspfAreaSummary, Long> {
    HasMetric(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An OspfAreaSummary with metric:", "metric");
    }

    @Override
    protected Long featureValueOf(OspfAreaSummary actual) {
      return actual.getMetric();
    }
  }

  static final class IsAdvertised extends FeatureMatcher<OspfAreaSummary, Boolean> {
    IsAdvertised(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An OspfAreaSummary with advertised:", "advertised");
    }

    @Override
    protected Boolean featureValueOf(OspfAreaSummary arg0) {
      return arg0.isAdvertised();
    }
  }

  static final class InstallsDiscard extends FeatureMatcher<OspfAreaSummary, Boolean> {
    InstallsDiscard(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An OspfAreaSummary that installs discard:", "installs discard");
    }

    @Override
    protected Boolean featureValueOf(OspfAreaSummary arg0) {
      return arg0.installsDiscard();
    }
  }

  private OspfAreaSummaryMatchersImpl() {}
}
