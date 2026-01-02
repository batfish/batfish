package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AaaAuthenticationLoginList;
import org.batfish.datamodel.AuthenticationMethod;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AaaAuthenticationLoginListMatchersImpl {

  static final class HasMethods
      extends FeatureMatcher<AaaAuthenticationLoginList, List<AuthenticationMethod>> {
    HasMethods(@Nonnull Matcher<? super List<AuthenticationMethod>> subMatcher) {
      super(subMatcher, "a login list has methods", "methods");
    }

    @Override
    protected List<AuthenticationMethod> featureValueOf(AaaAuthenticationLoginList loginList) {
      return loginList.getMethods();
    }
  }
}
