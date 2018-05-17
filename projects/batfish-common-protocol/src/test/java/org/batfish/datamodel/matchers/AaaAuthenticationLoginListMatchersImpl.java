package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLoginList;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class AaaAuthenticationLoginListMatchersImpl {

  static final class HasMethods extends FeatureMatcher<AaaAuthenticationLoginList, List<String>> {
    HasMethods(@Nonnull Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "a login list has methods", "methods");
    }

    @Override
    protected List<String> featureValueOf(AaaAuthenticationLoginList loginList) {
      return loginList.getMethods();
    }
  }

  static final class HasMethod extends TypeSafeMatcher<AaaAuthenticationLoginList> {
    private final String _method;

    HasMethod(@Nonnull String method) {
      _method = method;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("a login list has method '%s'", _method));
    }

    @Override
    protected boolean matchesSafely(AaaAuthenticationLoginList loginList) {
      return loginList.getMethods().contains(_method);
    }
  }
}
