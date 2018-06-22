package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import java.util.List;
import org.batfish.datamodel.AuthenticationMethod;
import org.batfish.datamodel.matchers.AaaAuthenticationLoginListMatchersImpl.HasMethods;
import org.hamcrest.Matcher;

public class AaaAuthenticationLoginListMatchers {

  public static HasMethods hasMethod(AuthenticationMethod method) {
    return new HasMethods(hasItem(equalTo(method)));
  }

  public static HasMethods hasMethods(Matcher<? super List<AuthenticationMethod>> subMatcher) {
    return new HasMethods(subMatcher);
  }
}
