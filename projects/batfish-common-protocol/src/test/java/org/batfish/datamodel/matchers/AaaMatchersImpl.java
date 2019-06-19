package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.vendor_family.cisco.Aaa;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthentication;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AaaMatchersImpl {

  static final class HasAuthentication extends FeatureMatcher<Aaa, AaaAuthentication> {
    HasAuthentication(@Nonnull Matcher<? super AaaAuthentication> subMatcher) {
      super(subMatcher, "a aaa with authentication", "authentication");
    }

    @Override
    protected AaaAuthentication featureValueOf(Aaa actual) {
      return actual.getAuthentication();
    }
  }
}
