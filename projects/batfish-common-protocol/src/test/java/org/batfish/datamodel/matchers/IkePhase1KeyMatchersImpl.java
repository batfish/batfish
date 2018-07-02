package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IkePhase1Key;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IkePhase1KeyMatchersImpl {

  static final class HasKey extends FeatureMatcher<IkePhase1Key, String> {
    HasKey(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IKE phase 1 key with key:", "key");
    }

    @Override
    protected String featureValueOf(IkePhase1Key actual) {
      return actual.getKey();
    }
  }
}
