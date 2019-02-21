package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.eigrp.EigrpProcessMode;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class EigrpProcessMatchersImpl {

  private EigrpProcessMatchersImpl() {}

  static final class HasAsn extends FeatureMatcher<EigrpProcess, Long> {
    HasAsn(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An EigrpProcess with asn:", "asn");
    }

    @Override
    protected Long featureValueOf(EigrpProcess actual) {
      return actual.getAsn();
    }
  }

  static final class HasMode extends FeatureMatcher<EigrpProcess, EigrpProcessMode> {
    HasMode(@Nonnull Matcher<? super EigrpProcessMode> subMatcher) {
      super(subMatcher, "An EigrpProcess with mode:", "mode");
    }

    @Override
    protected EigrpProcessMode featureValueOf(EigrpProcess actual) {
      return actual.getMode();
    }
  }

  static final class HasRouterId extends FeatureMatcher<EigrpProcess, Ip> {
    HasRouterId(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An EigrpProcess with routerId:", "routerId");
    }

    @Override
    protected Ip featureValueOf(EigrpProcess actual) {
      return actual.getRouterId();
    }
  }
}
