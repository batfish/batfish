package org.batfish.datamodel.matchers;

import org.batfish.datamodel.matchers.AaaMatchersImpl.HasAuthentication;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthentication;
import org.hamcrest.Matcher;

public class AaaMatchers {

  public static HasAuthentication hasAuthentication(Matcher<? super AaaAuthentication> subMatcher) {
    return new HasAuthentication(subMatcher);
  }
}
