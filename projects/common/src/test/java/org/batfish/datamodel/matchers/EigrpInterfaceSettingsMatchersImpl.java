package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.eigrp.EigrpInterfaceSettings;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class EigrpInterfaceSettingsMatchersImpl {
  static final class HasAsn extends FeatureMatcher<EigrpInterfaceSettings, Long> {
    HasAsn(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An EigrpInterfaceSettings with asn:", "asn");
    }

    @Override
    protected @Nonnull Long featureValueOf(EigrpInterfaceSettings actual) {
      return actual.getAsn();
    }
  }

  static final class HasEnabled extends FeatureMatcher<EigrpInterfaceSettings, Boolean> {
    HasEnabled(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An EigrpInterfaceSettings with enabled:", "enabled");
    }

    @Override
    protected @Nonnull Boolean featureValueOf(EigrpInterfaceSettings actual) {
      return actual.getEnabled();
    }
  }

  static final class HasEigrpMetric extends FeatureMatcher<EigrpInterfaceSettings, EigrpMetric> {
    HasEigrpMetric(@Nonnull Matcher<? super EigrpMetric> subMatcher) {
      super(subMatcher, "An EigrpInterfaceSettings with metric:", "metric");
    }

    @Override
    protected @Nonnull EigrpMetric featureValueOf(EigrpInterfaceSettings actual) {
      return actual.getMetric();
    }
  }

  static final class HasPassive extends FeatureMatcher<EigrpInterfaceSettings, Boolean> {
    HasPassive(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An EigrpInterfaceSettings with passive:", "passive");
    }

    @Override
    protected @Nonnull Boolean featureValueOf(EigrpInterfaceSettings actual) {
      return actual.getPassive();
    }
  }
}
