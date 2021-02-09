package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.AllowRemoteAsOutMode;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class AddressFamilyCapabilitiesMatchersImpl {

  static final class HasAllowLocalAsIn extends FeatureMatcher<AddressFamilyCapabilities, Boolean> {
    HasAllowLocalAsIn(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AddressFamilyCapabilities with allowLocalAsIn:", "allowLocalAsIn");
    }

    @Override
    protected Boolean featureValueOf(AddressFamilyCapabilities actual) {
      return actual.getAllowLocalAsIn();
    }
  }

  static final class HasAllowRemoteAsOut
      extends FeatureMatcher<AddressFamilyCapabilities, AllowRemoteAsOutMode> {
    HasAllowRemoteAsOut(@Nonnull Matcher<? super AllowRemoteAsOutMode> subMatcher) {
      super(subMatcher, "An AddressFamilyCapabilities with allowRemoteAsOut:", "allowRemoteAsOut");
    }

    @Override
    protected AllowRemoteAsOutMode featureValueOf(AddressFamilyCapabilities actual) {
      return actual.getAllowRemoteAsOut();
    }
  }

  static final class HasSendCommunity extends FeatureMatcher<AddressFamilyCapabilities, Boolean> {
    HasSendCommunity(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AddressFamilyCapabilities with sendCommunity:", "sendCommunity");
    }

    @Override
    protected Boolean featureValueOf(AddressFamilyCapabilities actual) {
      return actual.getSendCommunity();
    }
  }

  static final class HasSendExtendedCommunity
      extends FeatureMatcher<AddressFamilyCapabilities, Boolean> {
    HasSendExtendedCommunity(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(
          subMatcher,
          "An AddressFamilyCapabilities with sendExtendedCommunity:",
          "sendExtendedCommunity");
    }

    @Override
    protected Boolean featureValueOf(AddressFamilyCapabilities actual) {
      return actual.getSendExtendedCommunity();
    }
  }

  private AddressFamilyCapabilitiesMatchersImpl() {}
}
