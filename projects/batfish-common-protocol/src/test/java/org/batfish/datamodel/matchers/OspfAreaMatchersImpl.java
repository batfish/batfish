package org.batfish.datamodel.matchers;

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

final class OspfAreaMatchersImpl {

  static final class HasInjectDefaultRoute extends FeatureMatcher<OspfArea, Boolean> {
    HasInjectDefaultRoute() {
      super(is(true), "an OspfArea with injectDefaultRoute", "injectDefaultRoute");
    }

    @Override
    protected Boolean featureValueOf(OspfArea actual) {
      return actual.getInjectDefaultRoute();
    }
  }

  static final class HasInterfaces extends FeatureMatcher<OspfArea, Set<String>> {
    HasInterfaces(@Nonnull Matcher<? super Set<String>> subMatcher) {
      super(subMatcher, "an OspfArea with interfaces:", "interfaces");
    }

    @Override
    protected Set<String> featureValueOf(OspfArea actual) {
      return actual.getInterfaces();
    }
  }

  static final class HasMetricOfDefaultRoute extends FeatureMatcher<OspfArea, Integer> {
    HasMetricOfDefaultRoute(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an OspfArea with metricOfDefaultRoute:", "metricOfDefaultRoute");
    }

    @Override
    protected Integer featureValueOf(OspfArea actual) {
      return actual.getMetricOfDefaultRoute();
    }
  }

  static final class HasNssa extends FeatureMatcher<OspfArea, NssaSettings> {
    public HasNssa(@Nonnull Matcher<? super NssaSettings> subMatcher) {
      super(subMatcher, "An OspfArea with nssa:", "nssa");
    }

    @Override
    protected NssaSettings featureValueOf(OspfArea actual) {
      return actual.getNssa();
    }
  }

  static final class HasStub extends FeatureMatcher<OspfArea, StubSettings> {
    public HasStub(@Nonnull Matcher<? super StubSettings> subMatcher) {
      super(subMatcher, "An OspfArea with stub:", "stub");
    }

    @Override
    protected StubSettings featureValueOf(OspfArea actual) {
      return actual.getStub();
    }
  }

  static final class HasStubType extends FeatureMatcher<OspfArea, StubType> {

    public HasStubType(@Nonnull Matcher<? super StubType> subMatcher) {
      super(subMatcher, "An OspfArea with stubType:", "stubType");
    }

    @Override
    protected StubType featureValueOf(OspfArea actual) {
      return actual.getStubType();
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

  private OspfAreaMatchersImpl() {}
}
