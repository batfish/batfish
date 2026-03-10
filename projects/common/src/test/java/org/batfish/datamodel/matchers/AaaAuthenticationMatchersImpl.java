package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthentication;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLogin;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AaaAuthenticationMatchersImpl {

  static final class HasLogin extends FeatureMatcher<AaaAuthentication, AaaAuthenticationLogin> {
    HasLogin(@Nonnull Matcher<? super AaaAuthenticationLogin> subMatcher) {
      super(subMatcher, "a AaaAuthentication with login", "login");
    }

    @Override
    protected AaaAuthenticationLogin featureValueOf(AaaAuthentication actual) {
      return actual.getLogin();
    }
  }
}
