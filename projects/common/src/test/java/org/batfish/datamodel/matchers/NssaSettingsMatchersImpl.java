package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.NssaSettings;
import org.batfish.datamodel.ospf.OspfDefaultOriginateType;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class NssaSettingsMatchersImpl {

  static final class HasDefaultOriginateType
      extends FeatureMatcher<NssaSettings, OspfDefaultOriginateType> {
    HasDefaultOriginateType(@Nonnull Matcher<? super OspfDefaultOriginateType> subMatcher) {
      super(subMatcher, "An NssaSettings with defaultOriginateType:", "defaultOriginateType");
    }

    @Override
    protected OspfDefaultOriginateType featureValueOf(NssaSettings actual) {
      return actual.getDefaultOriginateType();
    }
  }

  static final class HasSuppressType3 extends FeatureMatcher<NssaSettings, Boolean> {
    HasSuppressType3(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An NssaSettings with suppressType3:", "suppressType3");
    }

    @Override
    protected Boolean featureValueOf(NssaSettings actual) {
      return actual.getSuppressType3();
    }
  }

  static final class HasSuppressType7 extends FeatureMatcher<NssaSettings, Boolean> {
    HasSuppressType7(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An NssaSettings with suppressType7:", "suppressType7");
    }

    @Override
    protected Boolean featureValueOf(NssaSettings actual) {
      return actual.getSuppressType7();
    }
  }

  private NssaSettingsMatchersImpl() {}
}
