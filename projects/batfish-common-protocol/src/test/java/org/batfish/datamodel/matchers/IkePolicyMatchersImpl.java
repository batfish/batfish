package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IkePolicy;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IkePolicyMatchersImpl {

  static final class HasPresharedKeyHash extends FeatureMatcher<IkePolicy, String> {
    HasPresharedKeyHash(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IKE policy with preSharedKeyHash:", "preSharedKeyHash");
    }

    @Override
    protected String featureValueOf(IkePolicy actual) {
      return actual.getPreSharedKeyHash();
    }
  }
}
