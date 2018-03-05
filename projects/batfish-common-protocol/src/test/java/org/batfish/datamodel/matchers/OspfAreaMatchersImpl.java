package org.batfish.datamodel.matchers;

import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfAreaSummary;
import org.batfish.datamodel.Prefix;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class OspfAreaMatchersImpl {

  static final class HasInterfaces extends FeatureMatcher<OspfArea, Set<String>> {
    HasInterfaces(@Nonnull Matcher<? super Set<String>> subMatcher) {
      super(subMatcher, "an OspfArea with interfaces:", "interfaces");
    }

    @Override
    protected Set<String> featureValueOf(OspfArea actual) {
      return actual.getInterfaces();
    }
  }

  static final class HasSummary extends FeatureMatcher<OspfArea, OspfAreaSummary> {
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
