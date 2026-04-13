package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AaaAuthenticationLoginList;
import org.batfish.datamodel.AuthenticationMethod;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AaaAuthenticationLoginListMatchers {

  public static Matcher<AaaAuthenticationLoginList> hasMethod(AuthenticationMethod method) {
    return new HasMethods(hasItem(equalTo(method)));
  }

  public static Matcher<AaaAuthenticationLoginList> hasMethods(
      Matcher<? super List<AuthenticationMethod>> subMatcher) {
    return new HasMethods(subMatcher);
  }

  public static Matcher<AaaAuthenticationLoginList> hasMethods() {
    return new HasMethods(not(empty()));
  }

  private static final class HasMethods
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
