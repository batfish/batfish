package org.batfish.datamodel.matchers;

import org.batfish.datamodel.matchers.AaaAuthenticationMatchersImpl.HasLogin;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLogin;
import org.hamcrest.Matcher;

public class AaaAuthenticationMatchers {

  public static HasLogin hasLogin(Matcher<? super AaaAuthenticationLogin> subMatcher) {
    return new HasLogin(subMatcher);
  }
}
