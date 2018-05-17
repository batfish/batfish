package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLoginList;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class AaaAuthenticationLoginListMatchersImpl {

  static final class HasMethod extends TypeSafeMatcher<AaaAuthenticationLoginList> {
    private final String _method;

    HasMethod(@Nonnull String method) {
      _method = method;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("Login list has method '%s'", _method));
    }

    @Override
    protected boolean matchesSafely(AaaAuthenticationLoginList loginList) {
      return loginList.getMethods().contains(_method);
    }
  }
}
