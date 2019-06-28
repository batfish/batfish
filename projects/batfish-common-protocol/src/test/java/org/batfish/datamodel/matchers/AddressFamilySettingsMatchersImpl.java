package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.AddressFamilySettings;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class AddressFamilySettingsMatchersImpl {

  static final class HasAllowLocalAsIn extends FeatureMatcher<AddressFamilySettings, Boolean> {
    HasAllowLocalAsIn(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AddressFamilySettings with allowLocalAsIn:", "allowLocalAsIn");
    }

    @Override
    protected Boolean featureValueOf(AddressFamilySettings actual) {
      return actual.getAllowLocalAsIn();
    }
  }

  static final class HasAllowRemoteAsOut extends FeatureMatcher<AddressFamilySettings, Boolean> {
    HasAllowRemoteAsOut(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AddressFamilySettings with allowRemoteAsOut:", "allowRemoteAsOut");
    }

    @Override
    protected Boolean featureValueOf(AddressFamilySettings actual) {
      return actual.getAllowRemoteAsOut();
    }
  }

  static final class HasSendCommunity extends FeatureMatcher<AddressFamilySettings, Boolean> {
    HasSendCommunity(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AddressFamilySettings with sendCommunity:", "sendCommunity");
    }

    @Override
    protected Boolean featureValueOf(AddressFamilySettings actual) {
      return actual.getSendCommunity();
    }
  }

  private AddressFamilySettingsMatchersImpl() {}
}
